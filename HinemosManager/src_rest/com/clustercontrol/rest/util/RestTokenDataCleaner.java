/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * HttpBearer認証向けに保持しているToken管理テーブルのクリーニング処理.<br>
 * <br>
 * Rest-API向け<br>
 * 
 */
public class RestTokenDataCleaner  implements Runnable {

	private final ScheduledExecutorService _scheduler;

	public RestTokenDataCleaner() {
		_scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "RestTokenCleaningScheduler");
			}
		});
	}

	/**
	 * 機能を活性化させるメソッド
	 */
	public void start() {
		//30分間隔で定周期処理を起動する
		_scheduler.scheduleWithFixedDelay(this ,90L ,1800L ,TimeUnit.SECONDS);
	}

	/**
	 * 機能を非活性化させるメソッド
	 */
	public void shutdown() {
		_scheduler.shutdown();
	}

	/**
	 * 定期実行間隔(interval)に基づいて、定期的に実行されるメソッド
	 */
	@Override
	public void run() {
		RestHttpBearerAuthenticator.getInstance().removeExpireToken();
	}
}
