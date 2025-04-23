/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.jobmanagement.rpa.bean.RoboScreenshotInfo;

/**
 * スクリーンショット取得指示ファイル生成タスククラス
 */
public class ScreenshotFileObserveTask extends ObserveTask {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(ScreenshotFileObserveTask.class);
	/** スレッド名 */
	private static final String threadName = "ScreenshotFileObserver";

	/**
	 *  コンストラクタ
	 * @throws HinemosUnknown 
	 */
	public ScreenshotFileObserveTask() throws HinemosUnknown {
		super(threadName);
	}

	@Override
	public void run() {
		m_log.info("run() : start");
		while (!Thread.currentThread().isInterrupted()) {
			try {
				m_log.debug("run() : loop start.");

				// 指示ファイルが生成するまで待機
				RoboScreenshotInfo roboScreenshotInfo = roboFileManager.read(RoboScreenshotInfo.class, checkInterval);
				if (roboScreenshotInfo == null) {
					m_log.info("run() : roboScreenshotInfo is null");
					return; // 処理終了等で処理が中断された場合
				}
				m_log.debug("run() : roboScreenshotInfo=" + roboScreenshotInfo);

				try {
					if (!super.isAllowedToExecute(roboScreenshotInfo)) {
						continue;
					}
				} catch (InterruptedException e) {
					m_log.debug("run() : thread interrupted. e=" + e.getMessage(), e);
					return;
				}

				// 実行指示ファイルを削除
				roboFileManager.delete(RoboScreenshotInfo.class);
				// スクリーンショットの取得
				ScreenshotUtil.save(roboScreenshotInfo.getScreenshotFileName());
			} catch (IOException e) {
				m_log.error("run() : " + e.getMessage(), e);
			}
		}
	}
}
