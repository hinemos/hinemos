/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.queue.internal;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.util.Singletons;

/**
 * ジョブキュー関連の非同期タスクを実行するExecutorです。 
 */
public class JobQueueExecutor extends ThreadPoolExecutor {
	/**
	 * コンストラクタではなく、{@link Singletons#get(Class)}を使用してください。
	 */
	public JobQueueExecutor() {
		super(
				// Thread pool
				getThreadPoolSize(), getThreadPoolSize(),
				// Keep alive
				0L, TimeUnit.MILLISECONDS,
				// Queue
				new LinkedBlockingQueue<Runnable>(),
				// Thread factory
				new ThreadFactory() {
					private AtomicLong serial = new AtomicLong(1);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "JobQueueExecutor-" + serial.getAndIncrement());
					}
				});
	}

	private static int getThreadPoolSize() {
		return HinemosPropertyCommon.jobqueue_executor_threadPool_size.getIntegerValue();
	}
}