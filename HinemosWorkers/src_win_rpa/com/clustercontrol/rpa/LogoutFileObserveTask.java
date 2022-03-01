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
import com.clustercontrol.jobmanagement.rpa.bean.RoboLogoutInfo;
import com.clustercontrol.jobmanagement.rpa.util.CommandProxy;
import com.clustercontrol.util.CommandExecutor.CommandResult;

/**
 * ログアウト指示ファイル生成監視タスク
 */
public class LogoutFileObserveTask extends ObserveTask {
	/** ロガー */
	private static Log m_log = LogFactory.getLog(LogoutFileObserveTask.class);
	/** スレッド名 */
	private static final String threadName = "LogoutFileObserver";
	/** ログアウトを行うコマンド */
	private String command = RpaToolExecutorProperties.getProperty("logout.command");

	/**
	 *  コンストラクタ
	 */
	public LogoutFileObserveTask() {
		super(threadName);
	}

	@Override
	public void run() {
		m_log.info("run() : start");
		while (!Thread.currentThread().isInterrupted()) {
			try {
				// 指示ファイルが生成するまで待機
				RoboLogoutInfo roboLogoutInfo = roboFileManager.read(RoboLogoutInfo.class, checkInterval);
				if (roboLogoutInfo == null) {
					m_log.warn("run() : roboLogoutInfo is null");
					return; // 処理終了等で処理が中断された場合
				}
				m_log.debug("run() : " + roboLogoutInfo);
				// 実行指示ファイルを削除
				roboFileManager.delete(RoboLogoutInfo.class);
				// ログアウトを実行
				CommandResult ret = null;
				try {
					ret = CommandProxy.execute(command);
				} catch (HinemosUnknown e) {
					m_log.error("run() : command execution failed, " + e.getMessage(), e);
				}
				if (ret != null) {
					m_log.debug("run() : exitCode=" + ret.exitCode + ", stdout=" + ret.stdout + ", stderr=" + ret.stderr);
					// プロセスを終了
					System.exit(0);
				}
			} catch (IOException e) {
				m_log.error("run() : " + e.getMessage(), e);
			}
		}
	}
}
