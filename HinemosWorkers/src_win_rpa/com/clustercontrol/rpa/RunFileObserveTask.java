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

import com.clustercontrol.jobmanagement.rpa.bean.RoboRunInfo;
import com.clustercontrol.rpa.handler.AbstractHandler;
import com.clustercontrol.rpa.handler.DestroyProcessHandler;
import com.clustercontrol.rpa.handler.ResultFileHandler;
import com.clustercontrol.rpa.handler.RootHandler;

/**
 * シナリオ実行指示ファイル生成監視タスク
 */
public class RunFileObserveTask extends ObserveTask {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(RunFileObserveTask.class);
	/** スレッド名 */
	private static final String threadName = "RunFileObserver";

	/**
	 *  コンストラクタ
	 */
	public RunFileObserveTask() {
		super(threadName);
	}

	@Override
	public void run() {
		m_log.info("run() : start");
		RoboRunInfo roboRunInfo = null;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				// 指示ファイルが生成するまで待機
				roboRunInfo = roboFileManager.read(RoboRunInfo.class, checkInterval);
				if (roboRunInfo == null) {
					m_log.warn("run() : roboRunInfo is null");
					return; // 処理終了等で処理が中断された場合
				}
				m_log.debug("run() : " + roboRunInfo);
				// 実行指示ファイルを削除
				roboFileManager.delete(RoboRunInfo.class);
				// シナリオ実行後のハンドラを設定
				ScenarioExecutor executor = new ScenarioExecutor(buildHandler(roboRunInfo));
				// 処理を中断できるようにキャッシュに登録
				ExecutorCache.put(roboRunInfo, executor);
				boolean result = executor.execute(roboRunInfo);
				m_log.info("run() : result=" + result);
			} catch (IOException e) {
				m_log.error("run() : " + e.getMessage(), e);
			} finally {
				if (roboRunInfo != null) {
					ExecutorCache.remove(roboRunInfo);
				}
			}
		}
	}

	/**
	 * シナリオ実行後の処理を行うハンドラを生成します。
	 * 
	 * @param roboRunInfo
	 * @return
	 */
	private AbstractHandler buildHandler(RoboRunInfo roboRunInfo) {
		AbstractHandler handler = new RootHandler(roboRunInfo);
		// ジョブ停止時にRPAプロセスを終了する場合
		if (roboRunInfo.getDestroy()) {
			handler.add(new DestroyProcessHandler(roboRunInfo));
		}
		// 実行結果ファイルを生成する
		handler.add(new ResultFileHandler(roboRunInfo));
		m_log.debug("buildHandler() : " + handler);
		return handler;
	}
}
