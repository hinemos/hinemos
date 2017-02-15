/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.run.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

/**
 * 監視を並列実行するクラス<BR>
 * <p>
 *
 * @version 4.0.0
 * @since 2.4.0
 */
public class ParallelExecution {
	
	// ロガー
	private static Log log = LogFactory.getLog(ParallelExecution.class);
	
	private static final ParallelExecution _instance = new ParallelExecution();
	
	private final ExecutorService es;

	/**
	 * コンストラクタ
	 */
	private ParallelExecution() {
		log.debug("init()");
		
		int m_maxThreadPool = HinemosPropertyUtil.getHinemosPropertyNum("monitor.common.thread.pool", Long.valueOf(10)).intValue();
		log.info("monitor.common.thread.pool: " + m_maxThreadPool);
		
		es = new MonitoredThreadPoolExecutor(m_maxThreadPool, m_maxThreadPool,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(),
				new ThreadFactory() {
			private volatile int _count = 0;
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "MonitorWorker-" + _count++);
			}
		}, new ThreadPoolExecutor.AbortPolicy());
		
		log.debug("ParallelExecution() ExecutorService is " + es.getClass().getCanonicalName());
		log.debug("ParallelExecution() securityManager is " + System.getSecurityManager());
	}
	
	public static ParallelExecution instance() {
		return _instance;
	}
	
	public ExecutorService getExecutorService() {
		return es;
	}
	
}
