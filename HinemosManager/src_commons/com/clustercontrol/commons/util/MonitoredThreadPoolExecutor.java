/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.bean.ThreadInfo;
import com.clustercontrol.util.HinemosTime;

/**
 * セルフチェック機能による活動状況の監視対象となるスレッドプールクラス<br/>
 */
public class MonitoredThreadPoolExecutor extends ThreadPoolExecutor {

	private static final Log log = LogFactory.getLog(MonitoredThreadPoolExecutor.class);

	private static Map<Long, ThreadInfo> runningTaskMap = new ConcurrentHashMap<Long, ThreadInfo>();

	public MonitoredThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	public MonitoredThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	}

	public MonitoredThreadPoolExecutor(int corePoolSize,
			int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue,
			RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	public MonitoredThreadPoolExecutor(int corePoolSize,
			int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
			RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
				threadFactory, handler);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		try {
			beginTask(t, r.getClass().getName());
		} finally {
			super.beforeExecute(t, r);
		}
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		try {
			finishTask(Thread.currentThread());
		} finally {
			super.afterExecute(r, t);
		}
	}

	public static void beginTask(Thread t, String className) {
		// ThreadLocalの初期化
		HinemosSessionContext.instance().setProperty(JpaTransactionManager.EM, null);

		// 実行を開始するタスク情報の記録
		ThreadInfo threadInfo = new ThreadInfo(t, className, HinemosTime.currentTimeMillis());
		runningTaskMap.put(t.getId(), threadInfo);

		if (log.isDebugEnabled()) {
			log.debug("starting new monitored task : " + threadInfo);
		}
	}

	public static void finishTask(Thread t) {
		// 実行を完了したタスク情報の削除
		ThreadInfo threadInfo = runningTaskMap.remove(t.getId());

		if (log.isDebugEnabled()) {
			log.debug("finishing new monitored task : " + threadInfo);
		}
	}

	public static Map<Long, ThreadInfo> getRunningThreadMap() {
		return Collections.unmodifiableMap(runningTaskMap);
	}

}
