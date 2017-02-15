package com.clustercontrol.plugin.util.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.plugin.factory.ModifyDbmsScheduler;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.util.HinemosTime;

/**
 * 時間がきたジョブを実行するスケジューラのコア実装部分<br>
 * <br>
 * == 実装の概要 ==<br>
 * startを呼び出すことでスケジューラごとにメインのスレッドが1つ起動する。
 * スケジューラのメインスレッドは、DelayQueue（時間を迎えたら取り出せるキュー）から
 * 実行対象ジョブを取り出し、順次実行していく。<br>
 * スケジュールを登録する際は、DelayQueueへの挿入に加え、Mapにも登録する。
 * 登録済みのジョブの検索・変更時にはJobKeyを使ってMapからスケジュールを取り出す。<br>
 * <br>
 * == スレッドセーフ性に関する実装方針 ==<br>
 * HinemosSchedulerをラップするSchedulerPlugin側で同期・排他処理を行っているため、
 * 基本的には本クラス内での同期・排他処理は行わない
 */
public final class HinemosScheduler {
	
	private static Log m_log = LogFactory.getLog( HinemosScheduler.class );
	
	private static class Task implements Runnable {
		private final JobWrapper wrapper;
		private volatile Future<?> future = null;

		public Task(JobWrapper wrapper) {
			this.wrapper = wrapper;
		}
		
		public Future<?> getFuture() {
			return future;
		}
		public void setFuture(Future<?> future) {
			this.future = future;
		}
		
		@Override
		public void run() {
			try {
				Job job = wrapper.getDetail().getJobClass().newInstance();
				if (m_log.isDebugEnabled()) m_log.debug("run() : execute() ID="+ wrapper.getDetail().getName());
				job.execute(wrapper.getDetail());
				wrapper.notifySuccess(this);
			} catch (Exception e) {
				wrapper.notifyError(this);
				if (e instanceof RuntimeException) {
					throw (RuntimeException)e;
				} else {
					throw new RuntimeException(e);
				}
			} finally {
			}
		}
	}
	
	/**
	 * 個々のスケジュールで必要な情報をまとめたクラス。
	 * このインスタンスをスケジューラのQueueに入れて、fireTimeを迎えたものから実行する。
	 * このクラスはおおよそ以下の情報を保持している。
	 * 
	 * - JobDetail fire時に実行すべきコールバックメソッドなどの情報
	 * - Trigger トリガ情報（キックのタイミングや定期的なスケジュールかなどの情報）
	 * - Task キックされたスレッドを制御するためのFutureなど
	 */
	private static class JobWrapper implements Delayed {
		private final HinemosScheduler scheduler;
		private final JobDetail detail;
		private final Trigger trigger;
		
		private TriggerState status = TriggerState.VIRGIN;

		private List<Task> executingTasks = new ArrayList<>();

		public JobWrapper(HinemosScheduler scheduler, JobDetail detail, Trigger trigger) {
			this.scheduler = scheduler;
			this.detail = detail;
			this.trigger = trigger;
		}

		public JobDetail getDetail() {
			return detail;
		}

		public Trigger getTrigger() {
			return trigger;
		}

		public synchronized TriggerState getStatus() {
			return status;
		}
		public synchronized void setStatus(TriggerState status) {
			this.status = status;
		}
		
		public synchronized void addTask(Task task) {
			executingTasks.add(task);
		}
		
		public synchronized void notifySuccess(Task task) {
			executingTasks.remove(task);

			if (status == TriggerState.CANCELLED)
				return;
			
			if (trigger.getNextFireTime() < 0) {
				status = TriggerState.EXECUTED;
				
				if(scheduler.getSchedulerType() == SchedulerPlugin.SchedulerType.DBMS){
					try {
						m_log.trace("notifySuccess() : modifyDbmsSchedulerInternal() call.");
						ModifyDbmsScheduler dbms = new ModifyDbmsScheduler();
						dbms.modifyDbmsSchedulerInternal(detail, trigger, status.name());
					} catch(Exception e) {
						m_log.error("modifyDbmsSchedulerInternal() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw  new RuntimeException(e);
					}
				}
			}
		}
		
		public synchronized void notifyError(Task task) {
			executingTasks.remove(task);
			
			if (status == TriggerState.CANCELLED)
				return;
			
			if (trigger.getNextFireTime() < 0) {
				status = TriggerState.ERROR;
				
				if(scheduler.getSchedulerType() == SchedulerPlugin.SchedulerType.DBMS){
					try {
						m_log.trace("notifyError() : modifyDbmsSchedulerInternal() call.");
						ModifyDbmsScheduler dbms = new ModifyDbmsScheduler();
						dbms.modifyDbmsSchedulerInternal(detail, trigger, status.name());
					} catch(Exception e) {
						m_log.error("modifyDbmsSchedulerInternal() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw  new RuntimeException(e);
					}
				}
			}
		}
		
		public synchronized void cancel() {
			if (status != TriggerState.SCHEDULED)
				return;
			
			for (Task task: executingTasks) {
				if (!task.getFuture().isDone()) {
					task.getFuture().cancel(true);
				}
			}
			
			executingTasks.clear();
			
			status = TriggerState.CANCELLED;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((scheduler == null) ? 0 : scheduler.hashCode());
			result = prime * result + ((detail == null) ? 0 : detail.hashCode());
			result = prime * result + ((trigger == null) ? 0 : trigger.hashCode());
			return result;
		}

		@Override
		public synchronized boolean equals(Object o) {
			if(o == null){
				return false;
			}
			if (!(o instanceof JobWrapper)){
				return false;
			}
			return this.compareTo((Delayed)o) == 0;
		}

		@Override
		public synchronized int compareTo(Delayed o) {
			long diff;
			
			// TODO HinemosScheduler関連クラスのhashCode, equals, compareToは整理が必要
			JobWrapper other = (JobWrapper)o;
			diff = ((AbstractTrigger)this.getTrigger()).compareTo((AbstractTrigger)(other.getTrigger()));
			if (0 < diff) {
				return 1;
			} else if (diff < 0){
				return -1;
			}
			return 0;
		}

		@Override
		public synchronized long getDelay(TimeUnit unit) {
			long delayMillisec = getTrigger().getNextFireTime() - HinemosTime.currentTimeMillis();
			return unit.convert(delayMillisec, TimeUnit.MILLISECONDS);
		}
	}

	private final DelayQueue<JobWrapper> schedulerQueue = new DelayQueue<>();
	private final Semaphore workerThreadSemaphore;
	private final ThreadPoolExecutor executor;
	
	private final ConcurrentMap<JobKey, JobWrapper> jobs = new ConcurrentHashMap<>(); 

	private volatile Thread thread;
	private volatile boolean isShutdown = false;
	
	private final SchedulerPlugin.SchedulerType schedulerType;
	private final int threshold;

	public HinemosScheduler(SchedulerPlugin.SchedulerType type) {
		
		schedulerType = type;
		threshold = schedulerType.getMisfireThreshold();
		workerThreadSemaphore = new Semaphore(schedulerType.getPoolSize());
		executor = new ThreadPoolExecutor(
				schedulerType.getPoolSize(), // corePoolSize
				schedulerType.getPoolSize(), // maximumPoolSize
				Long.MAX_VALUE, // keepAliveTime
				TimeUnit.DAYS, 
				new LinkedBlockingQueue<Runnable>(), // workQueue
				new ThreadFactory() {
					private final AtomicInteger counter = new AtomicInteger(0);
					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, schedulerType.getWorkerThreadNameBase() + counter.getAndIncrement());
					}
				}
		) {
			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				try {
					super.afterExecute(r, t);
				} finally {
					// Workerスレッドがプールに返却されるタイミングでSemaphoreをリリースする
					// （正確にはこのコードはWorkerスレッド自身によって実行されているため、まだworkerスレッドは
					// スレッドプールには返却されていない。が、この直後に即座にプールに返却されるため、
					// 実質的には返却されたタイミングといって問題ない）
					workerThreadSemaphore.release();
					
					if (m_log.isDebugEnabled()) {
						m_log.debug("afterExecute() : thread finished and release Semaphore. Permits=" + workerThreadSemaphore.availablePermits());
					}
				}
			}
		};
	}
	
	/**
	 * マネージャ起動時にDBMSスケジュール情報をスケジューラに登録する
	 * （DBに登録済みの内容なので、メモリへの展開のみを実施する）
	 * 
	 * 本関数はマネージャ起動時にのみ呼び出される。
	 * また、start処理によりスケジューラが活性化される前に呼び出すこと。
	 * 
	 * @param jobDetail
	 * @param trigger
	 * @param status
	 * @throws SchedulerException
	 */
	public void initDbmsScheduleJob(JobDetail jobDetail, Trigger trigger, String status) throws SchedulerException {
		JobWrapper wrapper = new JobWrapper(this, jobDetail, trigger);
		wrapper.setStatus(TriggerState.valueOf(status));
		if (m_log.isDebugEnabled()) {
			m_log.debug("initDbmsScheduleJob():status=" + status);
		}
		// schedulerQueueへの登録はRAM同様にstart()時に実施する
		jobs.put(wrapper.getDetail().getKey(), wrapper);
	}
	
	/**
	 * スケジューラのメインスレッドを活性化し、スケジューラを開始させる
	 * @param startDelaySec スケジューラの開始を遅延させる時間（秒）
	 * 
	 */
	public void start(final long startDelayMillis) {
		this.thread = new Thread() {
			@Override
			public void run() {
				mainLoop(startDelayMillis);
			}
		};
		this.thread.setName(schedulerType.getMainThreadName());
		this.thread.start();
		
		for (JobWrapper wrapper: jobs.values()) {
			// DBMS分はinitDbmsScheduleJob()で反映済みのため、RAM分のみTrigger情報を更新する
			if(schedulerType != SchedulerPlugin.SchedulerType.DBMS){
				if (wrapper.getStatus() != TriggerState.VIRGIN)
					throw new IllegalStateException("Task already scheduled or cancelled");
				wrapper.getTrigger().computeFirstFireTime(HinemosTime.currentTimeMillis());
				wrapper.setStatus(TriggerState.SCHEDULED);
			}
			// schedulerQueueへの登録はRAM/DBMS共にここで実施する
			schedulerQueue.add(wrapper);
		}
		m_log.debug("start() : scheduler start and notify to scheduler main thread.");
	}
	
	private void mainLoop(long startDelayMillis) {
		try {
			// 初期化遅延が存在する場合は遅延時間待つ
			if (startDelayMillis > 0) {
				Thread.sleep(startDelayMillis);
			}
			
			// キューからタスクを取り出し、ThreadPoolに処理委譲させるメインループ
			// （スケジューラのメインスレッドはこのループ内でジョブをディスパッチし続ける）
			while (true) {
				// workerスレッド数分確保されたSemaphoreを、workerスレッドを使用する前に確保する。
				// workerスレッドがプールに戻る際に、確保したSemaphoreは解放する。
				// いきなりスレッドプールに投げこんでしまうと、workerスレッドが全てbusy状態の場合に
				// ログを出すことができないため、まずはSemaphoreを時間制限付きで施行取得することで、
				// workerスレッドの全busy状態をロギング可能にする。
				if (m_log.isDebugEnabled()) m_log.debug("mainLoop() : tryAcquire Permits=" + workerThreadSemaphore.availablePermits());
				
				while (workerThreadSemaphore.tryAcquire(1, TimeUnit.MINUTES) == false) {
					m_log.warn("mainLoop() : all worker threads are busy. can not scheduler new task for 1 minutes. scheduler = " + schedulerType.name());
				}
				boolean taskSubmitted = false;
				while(taskSubmitted == false){
					if (m_log.isTraceEnabled()) {
						Iterator<JobWrapper> itr = schedulerQueue.iterator();
						m_log.trace("---1---");
						long minDelay = Long.MAX_VALUE;
						while (itr.hasNext()) {
							JobWrapper j = itr.next();
							long delay = j.getDelay(TimeUnit.MILLISECONDS);
							m_log.trace("queue : " + delay + ", " + j.getTrigger().getTriggerKey().toString());
							if (delay < minDelay) {
								minDelay = delay;
							}
						}
						m_log.trace("---2--- minDelay=" + minDelay);
					}
					
					// Semaphoreの取得ができたため、次はworkerに処理委譲するタスクをQueueから取得する
					// タスクの取得は時間制限なしで実施する (タスクが投げ込まれない状況は特に異常ではないので）
					JobWrapper fireTargetJob = schedulerQueue.take();
					m_log.trace("---3---");
					
					// キャンセル済みのジョブであれば何もしない
					if (fireTargetJob.getStatus() == TriggerState.CANCELLED) {
						m_log.debug("mainLoop() : cancel Job=" + fireTargetJob.detail.getName() + ", group=" + fireTargetJob.detail.getGroup());
						JobKey key = fireTargetJob.getDetail().getKey();
						if (key != null) {
							jobs.remove(key);
						}
						continue;
					}
					
					long currentTime = HinemosTime.currentTimeMillis();
					long nextFireTime = fireTargetJob.getTrigger().getNextFireTime();
					
					if (currentTime - nextFireTime < threshold) {
						if (m_log.isDebugEnabled()) m_log.debug("mainLoop() : Kick Job=" + fireTargetJob.detail.getName() + ", group=" + fireTargetJob.detail.getGroup());
						// 現在時刻が実行予定時刻からmisfireThreashold以内のパターン
						// この場合
						// - スケジュールジョブ（DBMS＋CronTrigger）
						//   → 1回実行し、次の実行は現在時刻以降で計画された時間とする
						// - 監視（RAM＋SimpleTrigger）
						//   → 計画されているものを順次全て実行する
						// として動く
						fireTargetJob.getTrigger().triggered(currentTime);
						if (m_log.isDebugEnabled()) m_log.debug("mainLoop() : PrevFireTime=" + fireTargetJob.getTrigger().getPreviousFireTime() + ", NextFireTime=" + fireTargetJob.getTrigger().getNextFireTime());
						Task task = new Task(fireTargetJob);
						fireTargetJob.addTask(task);
						Future<?> future = executor.submit(task);
						task.setFuture(future);
						taskSubmitted = true;
					} else {
						m_log.info("mainLoop() : Misfire Job=" + fireTargetJob.detail.getName() + ", group=" + fireTargetJob.detail.getGroup());
						// 実行予定時刻よりも現在時刻がmisfireThreshold以上過ぎてしまったパターン
						// この場合は
						// - スケジュールジョブ（DBMS＋CronTrigger）
						//   → 現在時刻以前のスケジュールは一切実行しない
						// - 監視（RAM＋SimpleTrigger）
						//   → どうしよう・・・
						// として動く
						fireTargetJob.getTrigger().updateAfterMisfire(currentTime);
						m_log.info("mainLoop() : PrevFireTime=" + fireTargetJob.getTrigger().getPreviousFireTime() + ", NextFireTime=" + fireTargetJob.getTrigger().getNextFireTime());
						// TODO 今のところ何もしない動きにしている(以前のスケジュールは実行せず、現在時刻基点で再スケジュール)
					}
					// Trigger情報更新
					if(schedulerType == SchedulerPlugin.SchedulerType.DBMS){
						try {
							m_log.trace("mainLoop() : modifyDbmsSchedulerInternal() call.");
							ModifyDbmsScheduler dbms = new ModifyDbmsScheduler();
							dbms.modifyDbmsSchedulerInternal(fireTargetJob.getDetail(), fireTargetJob.getTrigger(), fireTargetJob.getStatus().name());
						} catch(Exception e) {
							m_log.error("modifyDbmsSchedulerInternal() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
							throw  new SchedulerException(e);
						}
					}
					
					// 繰り返し実行するものかワンショットのスケジュールかを判断する
					// （但し現在のHinemosではワンショットのスケジュールは存在しない）
					if (fireTargetJob.getTrigger().getNextFireTime() > 0) {
						// 新しい時刻でスケジューラに再登録する
						schedulerQueue.add(fireTargetJob);
					} else {
						// ワンショットのスケジュールなので再登録は行わず、全ての情報を削除
						// （現在のHinemosではこのパスは通らないはず）
						JobKey key = fireTargetJob.getDetail().getKey();
						if (key != null) {
							jobs.remove(key);
						}
					}
				}
			}
		} catch (InterruptedException e) {
			// SemaphoreのtryAcquireやDelayQueue.take、Executor.submitでInterruptedExceptionが発生する可能性がある
			// この例外はJVMの完了を意味するため、スレッドにインタラプト状態をセットして即座に終了し、呼び出し元に伝播させる
			m_log.info("mainLoop() : thread interrupted. schedulerType = " + schedulerType.name());
			Thread.interrupted();
			return;
		} catch (Throwable e) {
			// 想定外の例外によりスケジューラが終了する場合は致命的なエラーとして出力する
			m_log.fatal("mainLoop() : unexpected error occured. schedulerType = " + schedulerType.name(), e);
			throw e;
		} finally {
			isShutdown = true;
			m_log.info("mainLoop() : scheduler service finished. schedulerType = " + schedulerType.name());
		}
	}

	public void scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
		if (jobs.containsKey(jobDetail.getKey()))
			throw new SchedulerException("Jobkey is already registerd");

		if (isShutdown)
			throw new IllegalStateException("Timer already cancelled.");

		JobWrapper wrapper = new JobWrapper(this, jobDetail, trigger);
		jobs.put(wrapper.getDetail().getKey(), wrapper);
		
		if (this.thread != null) {
			if (wrapper.getStatus() != TriggerState.VIRGIN)
				throw new IllegalStateException("Task already scheduled or cancelled");
			wrapper.getTrigger().computeFirstFireTime(HinemosTime.currentTimeMillis());
			wrapper.setStatus(TriggerState.SCHEDULED);
			// Trigger情報更新
			if(schedulerType == SchedulerPlugin.SchedulerType.DBMS){
				try {
					m_log.trace("scheduleJob() : modifyDbmsSchedulerInternal() call.");
					ModifyDbmsScheduler dbms = new ModifyDbmsScheduler();
					dbms.modifyDbmsSchedulerInternal(wrapper.getDetail(), wrapper.getTrigger(), wrapper.getStatus().name());
				} catch(Exception e) {
					m_log.error("modifyDbmsSchedulerInternal() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new SchedulerException(e);
				}
			}
			schedulerQueue.add(wrapper);
			if(jobs.size() != schedulerQueue.size()) {
				m_log.error("jobs.size=" + jobs.size() + ", queue.size=" + schedulerQueue.size());
			}
		}
	}
	
	public void deleteJob(JobKey key) {
		JobWrapper wrapper = jobs.get(key);
		if (wrapper == null)
			return;
		wrapper.cancel();
		boolean flag = schedulerQueue.remove(wrapper);
		if (!flag) {
			// ここには到達しないはず。
			m_log.error("schedulerQueue.remove=false!! " + wrapper + ", " + key);
		}
		if (schedulerQueue.contains(wrapper)) {
			// ここには到達しないはず。
			m_log.error("schedulerQueue.remove failure!! " + wrapper + ", " + key);
		}
		jobs.remove(key);
	}
	
	public void shutdown() {
		isShutdown = true;
		
		for (JobWrapper wrapper : jobs.values()) {
			wrapper.cancel();
		}
		schedulerQueue.clear();
		jobs.clear();
		Thread schedulerMainThread = this.thread;
		if (schedulerMainThread != null) {
			schedulerMainThread.interrupt();
		}
		m_log.debug("shutdown() : shutdown scheduler and notify to scheduler main thread.");
	}
	
	public Map<JobKey, Trigger> getAllTrigger() {
		Map<JobKey, Trigger> returnTriggers = new HashMap<>();
		for (Map.Entry<JobKey, JobWrapper> entry : jobs.entrySet()) {
			returnTriggers.put(entry.getKey(), entry.getValue().getTrigger());
		}
		return returnTriggers;
	}
	
	public int getTriggerSize() {
		return jobs.size();
	}
	
	public int getQueueSize() {
		return schedulerQueue.size();
	}
	
	public boolean isShutdown() {
		return isShutdown;
	}
	
	public TriggerState getTriggerState(JobKey key) {
		return jobs.get(key).getStatus();
	}
	
	public Trigger getTrigger(String name, String group) {
		JobWrapper wrapper = jobs.get(new JobKey(name, group));
		return wrapper != null ? wrapper.getTrigger(): null;
	}
	
	public SchedulerPlugin.SchedulerType getSchedulerType() {
		return schedulerType;
	}
	
	public boolean checkExists(JobKey key) {
		return jobs.containsKey(key);
	}
	
	public String getSchedulerName() {
		return this.getClass().getSimpleName();
	}
	
	protected void finalize() throws Throwable {
		shutdown();
		super.finalize();
	}
}