/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 指示ファイル生成監視タスク実行クラス
 */
public class ObserverExecutor {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(ObserverExecutor.class);
	/** スレッドプール */
	protected ExecutorService observer;
	/** 指示ファイル生成を検知した場合に実行するタスク */
	protected ObserveTask observeTask;

	public ObserverExecutor(final ObserveTask observeTask) {
		this.observer = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, observeTask.getThreadName());
				thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread t, Throwable e) {
						m_log.error("uncaughtException() : threadName=" + t.getName() + ", message=" + e.getMessage(),
								e);
					}
				});
				return thread;
			}
		});
		this.observeTask = observeTask;
	}

	/**
	 * 処理を開始します。
	 */
	public void start() {
		m_log.info("start() : threadName=" + observeTask.getThreadName());
		observer.submit(observeTask);
	}

	/**
	 * 処理を終了します。
	 */
	public void shutdown() {
		m_log.info("shutdown() : threadName=" + observeTask.getThreadName());
		observeTask.abort();
		observer.shutdownNow();
	}
}
