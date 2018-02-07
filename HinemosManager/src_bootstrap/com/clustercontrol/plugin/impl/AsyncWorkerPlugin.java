/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.HinemosManagerMain.StartupMode;
import com.clustercontrol.HinemosManagerMain.StartupTask;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.plugin.api.AsyncTaskFactory;
import com.clustercontrol.plugin.api.HinemosPlugin;
import com.clustercontrol.plugin.util.TaskExecutionAfterCommitCallback;

/**
 * 通知処理などの非同期処理の実行制御や永続化制御を管理するプラグインサービス<br/>
 */
public class AsyncWorkerPlugin implements HinemosPlugin {

	public static final Log log = LogFactory.getLog(AsyncWorkerPlugin.class);

	// 非同期処理の処理スレッド数（デフォルト値）
	public static final int _threadSizeDefault = 1;
	// 非同期処理の最大待ち処理数（デフォルト値）
	public static final int _queueSizeDefault = 20000;
	// 停止時に残留している処理の最大処理時間（デフォルト値）
	public static final int _shutdownTimeoutDefault = 10000;

	// 非同期処理のWorker一覧
	private static String[] workers = null;
	// 各Workerに対応するExecutor一覧
	private static final Map<String, ThreadPoolExecutor> _executorMap = new ConcurrentHashMap<String, ThreadPoolExecutor>();
	// 各Workerに対応するRunnable生成クラス一覧
	private static final Map<String, AsyncTaskFactory> _factoryMap = new ConcurrentHashMap<String, AsyncTaskFactory>();
	// 各Workerに対応する停止時最大処理時間の一覧
	private static final Map<String, Long> _shutdownTimeoutMap = new ConcurrentHashMap<String, Long>();
	// 各Workerに対応する払い出し処理IDの一覧
	private static final Map<String, Long> _nextTaskIdMap = new ConcurrentHashMap<String, Long>();

	// 排他制御用のLockオブジェクト
	private static final CountDownLatch _initializedLatch = new CountDownLatch(1);
	private static final Map<String, Object> _executorLock = new ConcurrentHashMap<String, Object>();
	private static final Map<String, Object> _counterLock = new ConcurrentHashMap<String, Object>();

	//workerの対象クラス
	public static final String NOTIFY_STATUS_TASK_FACTORY = "NotifyStatusTaskFactory";
	public static final String NOTIFY_JOB_TASK_FACTORY = "NotifyJobTaskFactory";
	public static final String NOTIFY_LOG_ESCALATION_TASK_FACTORY = "NotifyLogEscalationTaskFactory";
	public static final String NOTIFY_COMMAND_TASK_FACTORY = "NotifyCommandTaskFactory";
	public static final String NOTIFY_MAIL_TASK_FACTORY = "NotifyMailTaskFactory";
	public static final String NOTIFY_EVENT_TASK_FACTORY = "NotifyEventTaskFactory";
	public static final String CREATE_JOB_SESSION_TASK_FACTORY = "CreateJobSessionTaskFactory";
	public static final String NOTIFY_INFRA_TASK_FACTORY = "NotifyInfraTaskFactory";
	
	static {
		String workerList = HinemosPropertyCommon.worker_list.getStringValue();
		workers = workerList.split(",");
	}

	@Override
	public Set<String> getDependency() {
		Set<String> dependency = new HashSet<String>();
		dependency.add(Log4jReloadPlugin.class.getName());
		dependency.add(CacheInitializerPlugin.class.getName());
		return dependency;
	}

	@Override
	public Set<String> getRequiredKeys() {
		return null;
	}

	@Override
	public void create() {
	}

	@Override
	public void activate() {
		for (String worker : workers) {
			_executorLock.put(worker, new Object());
			_counterLock.put(worker, new Object());

			synchronized (_executorLock.get(worker)) {
				String defaultClassPrefix;
				defaultClassPrefix = "com.clustercontrol.notify.factory.";

				log.info("defaultClassPrefix=" + defaultClassPrefix);
				log.info("worker=" + worker);

				String className = null;
				if (worker.equals(CREATE_JOB_SESSION_TASK_FACTORY)) {
					className = HinemosPropertyCommon.worker_$_factoryclass.getStringValue(worker, "com.clustercontrol.jobmanagement.factory." + worker);
				} else {
					className = HinemosPropertyCommon.worker_$_factoryclass.getStringValue(worker, defaultClassPrefix + worker);
				}

				if (className == null || "".equals(className)) {
					log.warn("class not defined. (" + "worker." + worker + ".factoryclass" + ")");
				}
				try {
					Class<?> clazz = Class.forName(className);

					if (clazz.newInstance() instanceof AsyncTaskFactory) {
						AsyncTaskFactory taskFactory = (AsyncTaskFactory)clazz.newInstance();
						_factoryMap.put(worker, taskFactory);
						_nextTaskIdMap.put(worker, 0L);
						
						log.info("initialized task id for " + worker + " : " + HinemosManagerMain._instanceId);
					} else {
						log.warn("class is not sub class of AsyncTaskFactory. (" + className + ")");
						continue;
					}
				} catch (ClassNotFoundException e) {
					log.warn("class not found. (" + className + ")", e);
					continue;
				} catch (Exception e) {
					log.warn("instantiation failure. (" + className + ")", e);
					continue;
				}

				int threadSize;
				if (worker.equals(NOTIFY_STATUS_TASK_FACTORY ) 
						|| worker.equals(CREATE_JOB_SESSION_TASK_FACTORY)
						|| worker.equals(NOTIFY_EVENT_TASK_FACTORY )) {
					threadSize = HinemosPropertyCommon.worker_$_thread_size.getIntegerValue(worker, Long.valueOf(_threadSizeDefault));
				} else {
					threadSize = HinemosPropertyCommon.worker_$_thread_size.getIntegerValue(worker, Long.valueOf(8));
				}
				int queueSize = HinemosPropertyCommon.worker_$_queue_size.getIntegerValue(worker, Long.valueOf(_queueSizeDefault));

				log.info("activating asynchronous worker. (worker = " + worker + ", class = " + className +
						", threadSize = " + threadSize + ", queueSize = " + queueSize + ")");

				ThreadPoolExecutor executor = new MonitoredThreadPoolExecutor(threadSize, threadSize,
						0L, TimeUnit.MILLISECONDS,
						new LinkedBlockingQueue<Runnable>(queueSize),
						new AsyncThreadFactory(worker), new TaskRejectionHandler(worker));

				_executorMap.put(worker, executor);

				long shutdownTimeout= HinemosPropertyCommon.worker_$_shutdown_timeout.getNumericValue(worker, Long.valueOf(_shutdownTimeoutDefault));
				_shutdownTimeoutMap.put(worker, shutdownTimeout);
			}
		}
		
		_initializedLatch.countDown();
		
		for (String worker : workers) {
			if (HinemosManagerMain._startupMode != StartupMode.MAINTENANCE) {
				log.info("executing persisted task : " + worker);
				runPersistedTask(worker);
			}
		}
		
		if (HinemosManagerMain._startupMode == StartupMode.MAINTENANCE) {
			log.info("skipped persisted task execution (startup mode is MAINTENANCE)");
			HinemosManagerMain.addStartupTask(new AsyncWorkerStartupTask());
		}
	}

	public static class AsyncWorkerStartupTask implements StartupTask {
		
		@Override
		public void init() {
			for (String worker : workers) {
				synchronized (_executorLock.get(worker)) {
					log.info("executing persisted task : " + worker);
					runPersistedTask(worker);
				}
			}
		}
		
	}
	
	@Override
	public void deactivate() {
		if (workers != null) {
			for (String worker : workers) {
				log.info("stopping asynchronous worker. (worker = " + worker + ")");
				_executorMap.get(worker).shutdown();
				try {
					if (! _executorMap.get(worker).awaitTermination(_shutdownTimeoutMap.get(worker), TimeUnit.MILLISECONDS)) {
						List<Runnable> remained = _executorMap.get(worker).shutdownNow();
						if (remained != null) {
							log.info("shutdown timeout. runnable remained. (worker" + worker + ", size = " + remained.size() + ")");
						}
					}
				} catch (InterruptedException e) {
					_executorMap.get(worker).shutdownNow();
				}
			}
		}
	}

	@Override
	public void destroy() {

	}

	private static void runPersistedTask(String worker) {
		JpaTransactionManager tm = null;
		try {
			tm = new JpaTransactionManager();
			tm.begin();
		
			List<Serializable> params = AsyncTask.getRemainedParams(worker);
			log.info("running remained task : num = " + params.size());
	
			for (Serializable param : params) {
				try {
					if (log.isDebugEnabled()) {
						log.debug("running remained task. (worker = " + worker + ", param = " + param + ")");
					}
					addTask(worker, param, true);
				} catch (HinemosUnknown e) {
					log.warn(e.getMessage());
				}
			}
			
			tm.commit();
		} catch (Exception e) {
			log.warn("failure of executing remained tasks. (worker = " + worker + ")", e);
			if (tm != null)
				tm.rollback();
		} finally {
			if (tm != null)
				tm.close();
		}
	}

	private static long getNextTaskId(String worker) {
		synchronized (_counterLock.get(worker)) {
			long taskId = _nextTaskIdMap.get(worker);
			_nextTaskIdMap.put(worker, Long.MAX_VALUE - taskId < HinemosManagerMain._instanceCount ? 
					HinemosManagerMain._instanceId: taskId + HinemosManagerMain._instanceCount);
			if ((taskId - HinemosManagerMain._instanceId) % 1000 == 0) {
				log.info("asynchronomous worker statistics (worker = " + worker + ", count = " + taskId + ")");
			}
			return taskId;
		}
	}

	/**
	 * Hoge hoge = new Hoge();
	 * for (int i = 0; i < 10; i ++) {
	 *     hoge.set(i);
	 *     AsyncWorkerPlugin.addTask(A, hoge, b);
	 * }
	 * とするとバグります（元インスタンス（hoge）の変更の影響を受ける場合があります）。
	 * 下記のように、newしなおして別インスタンスとする必要があります。
	 * for (int i = 0; i < 10; i ++) {
	 *     Hoge hoge = new Hoge();
	 *     hoge.set(i);
	 *     AsyncWorkerPlugin.addTask(A, hoge, b);
	 * }
	 *
	 * @param worker
	 * @param param ここに入力するオブジェクトには要注意。
	 * @param persist
	 * @throws HinemosUnknown
	 */
	public static void addTask(String worker, Serializable param, boolean persist) throws HinemosUnknown {
		try {
			_initializedLatch.await();
		} catch (InterruptedException e) {
			throw new HinemosUnknown("interrupted initialization waiting. (worker = " + worker + ")", e);
		}
		
		AsyncTaskFactory factory = _factoryMap.get(worker);

		if (factory == null) {
			throw new HinemosUnknown("worker not found. (worker = " + worker + ")");
		}

		Runnable r = factory.createTask(param);

		long taskId = getNextTaskId(worker);
		if (persist && log.isDebugEnabled()) {
			log.debug("task will be persisted. (worker = " + worker + ", taskId = " + taskId + ", param = " + param + ")");
		}

		AsyncTask task = new AsyncTask(r, worker, param, taskId, persist);

		// commit成功後にworkerスレッドにタスクが割り当てる
		JpaTransactionManager tm = null;
		try {
			tm = new JpaTransactionManager();
			tm.begin();
			tm.addCallback(new TaskExecutionAfterCommitCallback(task));
			tm.commit();
		} catch (Exception e) {
			log.warn("task addition failure. (worker = " + worker + ", taskId = " + taskId + ", param = " + param + ")", e);
			if (tm != null)
				tm.rollback();
			
			throw new HinemosUnknown("task addition failure. (worker = " + worker + ", taskId = " + taskId + ", param = " + param + ")", e);
		} finally {
			if (tm != null)
				tm.close();
		}
	}

	public static void commitTaskExecution(AsyncTask task) {
		ThreadPoolExecutor executor = _executorMap.get(task._worker);

		synchronized (_executorLock.get(task._worker)) {
			executor.execute(task);
		}

		if (log.isDebugEnabled()) {
			log.debug("committed, task will be executed. (worker = " + task._worker + ", taskId = " + task._taskId + ", param = " + task._param + ")");
		}
	}

	public static String[] getWorkerList() {
		return workers;
	}

	public static int getTaskCount(String worker) throws HinemosUnknown {
		ThreadPoolExecutor executor = _executorMap.get(worker);

		if (executor == null) {
			throw new HinemosUnknown("worker thread is not initialized. (worker = " + worker + ")");
		}

		synchronized (_executorLock.get(worker)) {
			return executor.getQueue().size();
		}
	}

	private static class AsyncThreadFactory implements ThreadFactory {

		private final String _worker;
		private volatile int _count = 0;

		public AsyncThreadFactory(String worker) {
			this._worker = worker;
		}

		@Override
		public Thread newThread(Runnable r) {
			String threadName = "AsyncTask-" + _count++ + " [" + _worker + "]";
			return new Thread(r, threadName);
		}
	}

	private class TaskRejectionHandler extends ThreadPoolExecutor.DiscardPolicy {

		private final String _worker;

		public TaskRejectionHandler(String worker) {
			this._worker = worker;
		}

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
			log.warn("too many tasks are assigned to " + _worker + ". rejecting new task. : " + r + ".");
		}
	}

}
