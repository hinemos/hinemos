/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.fault.DbmsSchedulerNotFound;
import com.clustercontrol.plugin.factory.ModifyDbmsScheduler;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.apllog.AplLogger;

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
		
		private long postponedCount = 0;
		private long additionalDelay = 0;

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
				
				if( SchedulerPlugin.isDBMS(scheduler.getSchedulerType())){
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
				
				if( SchedulerPlugin.isDBMS(scheduler.getSchedulerType())){
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
			long delayMillisec = getTrigger().getNextFireTime() - HinemosTime.currentTimeMillis() + additionalDelay;
			return unit.convert(delayMillisec, TimeUnit.MILLISECONDS);
		}
		
		/**
		 * 実行予定時刻が少し後になるように、遅延時間を追加します。
		 * 
		 * @return true:後回しOK。false:後回し不可(上限回数に到達した)。
		 */
		// 必要かどうか分からないが念のため他メソッドに倣って synchronized とする
		public synchronized boolean postpone() {
			++postponedCount;
			long limit = HinemosPropertyCommon.scheduler_dbms_postpone_limit.getNumericValue();
			if (postponedCount < limit) {
				additionalDelay += HinemosPropertyCommon.scheduler_dbms_postpone_delay.getNumericValue();
				return true;
			} else {
				// 後回し回数には上限がある
				m_log.warn("JobWrapper.postpone: Exceeded the limit to postpone."
						+ " name=" + detail.getName() + ", group=" + detail.getGroup() + ", limit=" + limit);
				return false;
			}
		}
		
		/**
		 * {@link #postpone()} による後回し回数カウントと追加遅延時間をゼロに戻します。
		 */
		public synchronized void resetPostponing() {
			postponedCount = 0;
			additionalDelay = 0;
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
			if(SchedulerPlugin.isDBMS(schedulerType) == false){
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
			
			if (m_log.isDebugEnabled()) {
				m_log.debug("mainLoop() :  start . scheduler=" + schedulerType.name() + " ,threshold="+threshold + 
					" ,executor.maximumPoolSize=" + executor.getMaximumPoolSize() + " ,startDelayMillis="+startDelayMillis);
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
						removeFromJobs(fireTargetJob);
						continue;
					}
					
					// #10977: この時点で fireTargetJob に対応する DbmsSchedulerEntity がコミットされていないケースがある。
					// フェッチを試みて、見つからなければ後回しにしてキューへ戻す。
					if (SchedulerPlugin.isDBMS(schedulerType)) {
						try {
							QueryUtil.getDbmsSchedulerPK_NONE(fireTargetJob.getDetail().getName(), fireTargetJob.getDetail().getGroup());
						} catch (DbmsSchedulerNotFound dsnfe) {
							m_log.info("mainloop() : Failed to fetch. " + " Job=" + fireTargetJob.getDetail().getName() + ", group=" + fireTargetJob.getDetail().getGroup());
							if (fireTargetJob.postpone()) {
								schedulerQueue.add(fireTargetJob);
							} else {
								// 後回し上限を超えるような場合(デフォルトのHinemosプロパティ設定なら20秒待ってもDBが同期しない場合)は、
								// スケジュール登録後に例外が発生してDBのみロールバックしたなど、
								// 対応するエンティティが永久に見つからない状況であると考え、今後は処理しないように削除する。
								removeFromJobs(fireTargetJob);
							}
							continue;
						}
						// この fireTargetJob オブジェクトを再びキューに入れるので、後回しで追加した遅延があれば、
						// 次回以降へ影響しないようにリセットする必要がある。
						fireTargetJob.resetPostponing();
					}
					
					long currentTime = HinemosTime.currentTimeMillis();

					// SimpleIntervalTriggerの場合は、hinemos_reset_schedulerスクリプト実行直後、マネージャ起動時から再開する
					if (fireTargetJob.getTrigger() instanceof SimpleIntervalTrigger
							&& fireTargetJob.getTrigger().getNextFireTime() == 0L
							&& fireTargetJob.getTrigger().getPreviousFireTime() > 0L) {
						((SimpleIntervalTrigger)fireTargetJob.getTrigger()).setNextFireTime(currentTime);
					}

					long nextFireTime = fireTargetJob.getTrigger().getNextFireTime();

					// 前回実行時間を設定
					fireTargetJob.detail.setPreviousFireTime(fireTargetJob.getTrigger().getPreviousFireTime());

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
						fireTargetJob.detail.setExecuteTime(currentTime);
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
						// 以前のスケジュールは実行せず、現在時刻基点で再スケジュール
						
						// ユーザが気付けるよう、実行を見送った機能を通知する。
						if(HinemosPropertyCommon.scheduler_dbms_notify_delay.getBooleanValue()){
							// Hinemosプロパティで指定した機能のみ通知する。
							String target = HinemosPropertyCommon.scheduler_dbms_notify_delay_target.getStringValue();
							List<String> targetList = Arrays.asList(target.split(","));
							String groupName = fireTargetJob.detail.getGroup();
							
							if(targetList.contains(groupName)){
								String pluginId = null;
								if(HinemosModuleConstant.isExist(groupName)){
									pluginId = groupName;
								} else {
									//groupNameがプラグインIDに対応していない場合、HINEMOS_MANAGER_MONITORとする
									pluginId = HinemosModuleConstant.HINEMOS_MANAGER_MONITOR;
								}
								
								String[] args = { getSchedulerType().toString(), fireTargetJob.detail.getName(),
										groupName, String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", nextFireTime) };
								AplLogger.put(
										InternalIdCommon.SYS_SFC_SYS_026, pluginId,
										args);
							}
						}

					}
					// Trigger情報更新
					if(SchedulerPlugin.isDBMS(schedulerType)){
						try {
							m_log.trace("mainLoop() : modifyDbmsSchedulerInternal() call.");
							ModifyDbmsScheduler dbms = new ModifyDbmsScheduler();
							dbms.modifyDbmsSchedulerInternal(fireTargetJob.getDetail(), fireTargetJob.getTrigger(), fireTargetJob.getStatus().name());
						} catch (DbmsSchedulerNotFound e) {
							// キューから fireTargetJob を取り出して、この処理にたどり着くまでの間に、スケジュールが削除された場合に起こりうる
							m_log.warn("mainLoop() : Missing." + " job=" + fireTargetJob.getDetail().getName() + ", group=" + fireTargetJob.getDetail().getGroup());
							continue;
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
						removeFromJobs(fireTargetJob);
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

	private void removeFromJobs(JobWrapper jw) {
		JobKey key = jw.getDetail().getKey();
		if (key != null) {
			jobs.remove(key);
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
			if( SchedulerPlugin.isDBMS(schedulerType)){
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
				// jobsとschedulerQueue間で同期が取れていないため、タイミングによっては正常時でも以下のログは出力される。
				m_log.debug("jobs.size=" + jobs.size() + ", queue.size=" + schedulerQueue.size());
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
		if (executor != null) {
			executor.shutdown();
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
	
	@Override
	protected void finalize() throws Throwable {
		shutdown();
		super.finalize();
	}
}