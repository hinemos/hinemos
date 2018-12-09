/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.jobmanagement.RunInstructionInfo;
import com.clustercontrol.ws.jobmanagement.RunResultInfo;

/**
 * プロセスを終了するスレッドクラス<BR>
 * 
 * プロセスを終了させて、ジョブを停止させるスレッドです
 * 
 */
public class DeleteProcessThread extends AgentThread {

	// ロガー
	static private Log m_log = LogFactory.getLog(DeleteProcessThread.class);

	// ジョブ実行結果を受け取る際のエンコーディング
	private String m_inputEncoding = null;

	/**
	 * コンストラクタ
	 * 
	 * @param info
	 *            実行指示
	 * @param sendQueue
	 *            実行応答用メッセージ送信クラス
	 * @param runHistory
	 *            実行履歴
	 */
	public DeleteProcessThread(RunInstructionInfo info, SendQueue sendQueue) {
		super(info, sendQueue);

		// ログファイルのエンコーディングを設定
		m_inputEncoding =  AgentProperties.getProperty("job.stream.charset");
		if(m_inputEncoding == null){
			m_inputEncoding = System.getProperty("file.encoding");
		}
		m_log.info("job.encoding.stdstream = " + m_inputEncoding);
	}

	/**
	 * ジョブ（コマンド・スクリプト）を実行するクラス<BR>
	 * 
	 * ReceiveTopicで受け取ったジョブの指示が実行の場合に このメソッドが実行されます。
	 * 
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		m_log.debug("run start");

		Process process = null;

		//プロセス終了の場合
		process = RunHistoryUtil.findRunHistory(m_info);

		if (process == null) {
			// 実行履歴が存在しない場合にはエラーを返す
			m_log.warn("run() : process is null");
			RunResultInfo info = new RunResultInfo();
			info.setSessionId(m_info.getSessionId());
			info.setJobunitId(m_info.getJobunitId());
			info.setJobId(m_info.getJobId());
			info.setFacilityId(m_info.getFacilityId());
			info.setCommand(m_info.getCommand());
			info.setCommandType(m_info.getCommandType());
			info.setStopType(m_info.getStopType());
			info.setStatus(RunStatusConstant.ERROR);
			info.setTime(HinemosTime.getDateInstance().getTime());
			info.setEndValue(-1);
			info.setMessage("Internal Error : Ex. Agent restarted or Job already terminated.");
			info.setErrorMessage("");
			// 送信
			m_sendQueue.put(info);
			return;
		}

		// ---------------------------
		// -- 開始メッセージ送信
		// ---------------------------

		// メッセージ作成
		RunResultInfo info = new RunResultInfo();
		info.setSessionId(m_info.getSessionId());
		info.setJobunitId(m_info.getJobunitId());
		info.setJobId(m_info.getJobId());
		info.setFacilityId(m_info.getFacilityId());
		info.setCommand(m_info.getCommand());
		info.setCommandType(m_info.getCommandType());
		info.setStopType(m_info.getStopType());
		info.setStatus(RunStatusConstant.START);
		info.setTime(HinemosTime.getDateInstance().getTime());

		m_log.info("Process Delete SessionID=" + m_info.getSessionId() + ", JobID="
				+ m_info.getJobId());

		// 送信
		m_sendQueue.put(info);


		//プロセス終了の場合
		m_log.info("run() : shutdown process : " + process.toString());
		try {
			process.destroy();
			info.setEndValue(process.waitFor());
		} catch (Exception e) {
			m_log.warn("shutdown process : " + e.getMessage());
			
			// エラーを返す
			info.setTime(HinemosTime.getDateInstance().getTime());
			info.setEndValue(-1);
			info.setStatus(RunStatusConstant.ERROR);
			info.setMessage(e.getMessage());
			info.setErrorMessage("");
			m_sendQueue.put(info);
			return;
		}

		// ---------------------------
		// -- 終了メッセージ送信
		// ---------------------------

		info.setTime(HinemosTime.getDateInstance().getTime());
		info.setStatus(RunStatusConstant.END);
		m_sendQueue.put(info);

		////実行履歴から削除
		RunHistoryUtil.delRunHistory(m_info);

		m_log.debug("run end");
	}
}
