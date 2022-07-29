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
import com.clustercontrol.jobmanagement.rpa.bean.RoboAbortInfo;

/**
 * 中断指示ファイル生成監視タスククラス
 */
public class AbortFileObserveTask extends ObserveTask {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(AbortFileObserveTask.class);
	/** スレッド名 */
	private static final String threadName = "AbortFileObserver";

	/**
	 *  コンストラクタ
	 * @throws HinemosUnknown 
	 */
	public AbortFileObserveTask() throws HinemosUnknown {
		super(threadName);
	}

	@Override
	public void run() {
		m_log.info("run() : start");
		while (!Thread.currentThread().isInterrupted()) {
			m_log.debug("run() : loop start.");
			try {
				// 指示ファイルが生成するまで待機
				RoboAbortInfo roboAbortInfo = roboFileManager.read(RoboAbortInfo.class, checkInterval);
				if (roboAbortInfo == null) {
					m_log.warn("run() : roboAbortInfo is null");
					return; // 処理終了等で処理が中断された場合
				}
				m_log.debug("run() : roboAbortInfo=" + roboAbortInfo);

				try {
					if (!super.isAllowedToExecute(roboAbortInfo)) {
						continue;
					}
				} catch (InterruptedException e) {
					m_log.debug("run() : thread interrupted. e=" + e.getMessage(), e);
					return;
				}

				// 実行指示ファイルを削除
				roboFileManager.delete(RoboAbortInfo.class);
				// RPAシナリオ実行を中断
				ScenarioExecutor executor = ExecutorCache.get(roboAbortInfo);
				if (executor != null) {
					executor.abort();
				}
			} catch (IOException e) {
				m_log.error("run() : " + e.getMessage(), e);
			}
		}
	}
}
