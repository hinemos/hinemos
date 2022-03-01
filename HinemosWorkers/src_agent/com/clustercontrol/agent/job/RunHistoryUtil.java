/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.job;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;

import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;

/**
 * ジョブ実行履歴を管理するユーティリティクラス<BR>
 * 
 * エージェント側でもジョブ実行のステータスを管理します。<BR>
 * ジョブの実行前後処理としてハッシュテーブルに追加・削除を行います。
 * 
 */
public class RunHistoryUtil {

	//ロガー
	private static Log m_log = LogFactory.getLog(RunHistoryUtil.class);
	private static ConcurrentHashMap<String, RunHistoryWrapper> runHistory = new ConcurrentHashMap<String, RunHistoryWrapper>();

	/**
	 * 実行履歴を追加します。<BR>
	 * @param info
	 * @param process
	 */
	public static void addRunHistory(AgtRunInstructionInfoResponse info, Process process) {
		runHistory.put(getKey(info), new RunHistoryWrapper(process));
	}

	/**
	 * 実行履歴を追加します。<BR>
	 * 
	 * @param info
	 * @param thread
	 */
	public static void addRunHistory(AgtRunInstructionInfoResponse info, AgentThread thread) {
		runHistory.put(getKey(info), new RunHistoryWrapper(thread));
	}

	/**
	 * 実行履歴を削除します。<BR>
	 * @param info
	 */
	public static void delRunHistory(AgtRunInstructionInfoResponse info) {
		RunHistoryWrapper result = runHistory.remove(getKey(info));
		if (result == null) {
			m_log.info("delRunHistory() : RunHistory is already removed. key=" + getKey(info));
		}
	}

	/**
	 * 実行履歴を全て消去します。
	 * @param runHistory
	 */
	public static boolean clearRunHistory() {
		boolean flag = true;
		if (runHistory.isEmpty()) {
			flag = false;
		}
		runHistory.clear();
		return flag;
	}

	/**
	 * Processの実行履歴を検索します。<BR>
	 * 
	 * @param info
	 * @return process
	 */
	public static Process findProcessRunHistory(AgtRunInstructionInfoResponse info) {
		RunHistoryWrapper history = runHistory.get(getKey(info));
		if (history != null && history.getProcess() != null) {
			return history.getProcess();
		}
		return null;
	}

	/**
	 * Threadの実行履歴を検索します。<BR>
	 * 
	 * @param info
	 * @return AgentThread
	 */
	public static AgentThread findThreadRunHistory(AgtRunInstructionInfoResponse info) {
		RunHistoryWrapper history = runHistory.get(getKey(info));
		if (history != null && history.getThread() != null) {
			return history.getThread();
		}
		return null;
	}

	/**
	 * 実行履歴を検索します。<BR>
	 * このメソッドはWrapperをそのまま返します。<BR>
	 * 
	 * @param info
	 * @return RunHistoryWrapper
	 */
	public static RunHistoryWrapper findRunHistory(AgtRunInstructionInfoResponse info) {
		return runHistory.get(getKey(info));
	}

	/**
	 * 実行履歴をログに出力
	 * @return
	 */
	public static void logHistory() {
		try {
			for(String key : runHistory.keySet()) {
				m_log.info("A running job is out of control due to stopped agent : " + key);
			}
		} catch (ConcurrentModificationException e) {
			m_log.warn("Log output process is stopped due to job execution history updated at the same time.");
		}
	}

	/**
	 * ハッシュテーブルに格納するため実行履歴Keyを作成します。
	 * @param info
	 * @return
	 */
	public static String getKey(AgtRunInstructionInfoResponse info) {
		// DESTROY_PROCESSでは、NORMALで実行したプロセスを取得する必要があるためCommandTypeを変換する
		int commandType;
		if (info.getCommandType() == CommandTypeConstant.STOP &&
				info.getStopType() != null &&
				info.getStopType() == CommandStopTypeConstant.DESTROY_PROCESS) {
			commandType = CommandTypeConstant.NORMAL;
		} else {
			commandType = info.getCommandType();
		}
		return info.getSessionId() + "," + info.getJobId() + "," + commandType
				+ "," + info.getFacilityId();
	}

	/**
	 * 実行履歴に入れるダミーのプロセスを生成します
	 * @return process
	 */
	public static Process dummyProcess() {
		return new Process() {

			@Override
			public int waitFor() throws InterruptedException {
				return 0;
			}

			@Override
			public OutputStream getOutputStream() {
				return null;
			}

			@Override
			public InputStream getInputStream() {
				return null;
			}

			@Override
			public InputStream getErrorStream() {
				return null;
			}

			@Override
			public int exitValue() {
				return 0;
			}

			@Override
			public void destroy() {
			}
		};
	}

	/**
	 * ジョブ実行履歴を管理するためのラッパークラス<BR>
	 * 
	 * Processに依存しないジョブのために、Threadクラスを利用した履歴管理も可能とする<BR>
	 */
	public static class RunHistoryWrapper {
		private Process process = null;
		private AgentThread thread = null;

		/**
		 * コンストラクタ
		 * 
		 * @param process
		 */
		public RunHistoryWrapper(Process process) {
			this.process = process;
		}

		/**
		 * コンストラクタ
		 * 
		 * @param thread
		 */
		public RunHistoryWrapper(AgentThread thread) {
			this.thread = thread;
		}

		public Process getProcess() {
			return process;
		}

		public AgentThread getThread() {
			return thread;
		}
	}
}
