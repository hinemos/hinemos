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

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.rest.session.RestControllerBean;

/**
 * エージェントからのリクエスト重複防止向けに保持しているリクエスト管理テーブルのクリーニング処理.<br>
 * <br>
 * Rest-API向け<br>
 * 
 */

public class RestAgentRequestDataCleaner implements Runnable {

	private final ScheduledExecutorService _scheduler;
	private Long keepTimeMillis = 1000 * 60 * 30L ;

	public RestAgentRequestDataCleaner() {
		_scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "RestAgentRequestCleaningScheduler");
			}
		});
		int request_id_keep_minutes = HinemosPropertyCommon.rest_agent_request_id_keep_minutes.getIntegerValue(); 
		this.keepTimeMillis = Long.valueOf(request_id_keep_minutes * 60L * 1000L);
	}

	/**
	 * 機能を活性化させるメソッド
	 */
	public void start() {
		// リクエストレコードの保持時間と同じ間隔で定周期処理を起動する
		_scheduler.scheduleWithFixedDelay(this, 90L, keepTimeMillis/1000, TimeUnit.SECONDS);
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
		new RestControllerBean().discardRestAgentRequest(keepTimeMillis);
	}

}
