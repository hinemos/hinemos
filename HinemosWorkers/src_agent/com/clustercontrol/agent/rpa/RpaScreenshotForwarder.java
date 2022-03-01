/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.ForwardRpaScreenshotRequest;

import com.clustercontrol.agent.AgentRpaRestClientWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;

/**
 * スクリーンショットをマネージャへ転送するクラス
 */
public class RpaScreenshotForwarder {
	/** ロガー */
	private static Log log = LogFactory.getLog(RpaScreenshotForwarder.class);
	/** インスタンス */
	private static final RpaScreenshotForwarder _instance = new RpaScreenshotForwarder();
	/** スレッド名 */
	private static final String threadName = "RpaScreenshot";
	/** スレッドプール */
	private final ExecutorService executor;

	/**
	 * インスタンスを返します。
	 * 
	 * @return
	 */
	public static RpaScreenshotForwarder getInstance() {
		return _instance;
	}

	/**
	 * スクリーンショット転送処理
	 */
	private static class RpaScreenshotForwardTask implements Runnable {

		private ForwardRpaScreenshotRequest request;
		private File file;

		public RpaScreenshotForwardTask(ForwardRpaScreenshotRequest request, File file) {
			this.request = request;
			this.file = file;
		}

		@Override
		public void run() {
			try {
				try {
					AgentRpaRestClientWrapper.forwardRpaScreenshot(request, file);
					log.debug("run() : forward screenshot succeeded");
				} catch (InvalidRole | InvalidUserPass | InvalidSetting | MonitorNotFound | RestConnectFailed
						| HinemosUnknown e) {
					log.error("run() : failed to forward screenshot, " + e.getMessage(), e);
				} finally {
					// ファイルが残り続けるのを防ぐため、マネージャへの転送の成否に依らずファイルは削除する
					Files.delete(file.toPath());
				}
			} catch (IOException e) {
				log.warn("run() : failed to delete screenshot file, " + e.getMessage(), e);
			}
		}

	}

	/**
	 * コンストラクタ
	 */
	public RpaScreenshotForwarder() {
		executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, threadName);
				t.setDaemon(true);
				return t;
			}
		});
	}

	/**
	 * 転送するスクリーンショットファイルを追加します。
	 * 
	 * @param request
	 *            スクリーンショット情報DTO
	 * @param file
	 *            スクリーンショットのファイル
	 */
	public void add(ForwardRpaScreenshotRequest request, File file) {
		executor.submit(new RpaScreenshotForwardTask(request, file));
	}
}
