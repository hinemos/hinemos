/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.job;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.openapitools.client.model.SetJobResultRequest;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.JobResultSendableObject;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.util.HinemosTime;

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
	 *			  実行指示
	 * @param sendQueue
	 *			  実行応答用メッセージ送信クラス
	 * @param runHistory
	 *			  実行履歴
	 */
	public DeleteProcessThread(AgtRunInstructionInfoResponse info, SendQueue sendQueue) {
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
		process = RunHistoryUtil.findProcessRunHistory(m_info);

		if (process == null) {
			// 実行履歴が存在しない場合にはエラーを返す
			m_log.warn("run() : process is null");
			JobResultSendableObject sendme = new JobResultSendableObject();
			sendme.sessionId = m_info.getSessionId();
			sendme.jobunitId = m_info.getJobunitId();
			sendme.jobId = m_info.getJobId();
			sendme.facilityId = m_info.getFacilityId();
			sendme.body = new SetJobResultRequest();
			sendme.body.setCommand(m_info.getCommand());
			sendme.body.setCommandType(m_info.getCommandType());
			sendme.body.setStopType(m_info.getStopType());
			sendme.body.setStatus(RunStatusConstant.ERROR);
			sendme.body.setTime(HinemosTime.getDateInstance().getTime());
			sendme.body.setEndValue(-1);
			sendme.body.setMessage("Internal Error : Ex. Agent restarted or Job already terminated.");
			sendme.body.setErrorMessage("");
			// 送信
			m_sendQueue.put(sendme);
			return;
		}

		// ---------------------------
		// -- 開始メッセージ送信
		// ---------------------------

		// メッセージ作成
		JobResultSendableObject sendme = new JobResultSendableObject();
		sendme.sessionId = m_info.getSessionId();
		sendme.jobunitId = m_info.getJobunitId();
		sendme.jobId = m_info.getJobId();
		sendme.facilityId = m_info.getFacilityId();
		sendme.body = new SetJobResultRequest();
		sendme.body.setCommand(m_info.getCommand());
		sendme.body.setCommandType(m_info.getCommandType());
		sendme.body.setStopType(m_info.getStopType());
		sendme.body.setStatus(RunStatusConstant.START);
		sendme.body.setTime(HinemosTime.getDateInstance().getTime());

		m_log.info("Process Delete SessionID=" + m_info.getSessionId() + ", JobID="
				+ m_info.getJobId());

		// 送信
		m_sendQueue.put(sendme);


		//プロセス終了の場合
		m_log.info("run() : shutdown process : " + process.toString());
		try {
			String mode = AgentProperties.getProperty("job.command.mode");
			// sudoの仕様変更対応に伴い、
			// 実効ユーザが「ユーザを指定する」である場合は強制停止を行う
			if (!mode.equals("compatible") && m_info.getSpecifyUser() && process.getClass().getName().equals("java.lang.UNIXProcess")){
				// sudoプロセスID取得
				Field f = process.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				int pid = (int)f.get(process);
				m_log.debug("run() : PID = "+ pid);

				// sudoの子プロセスID取得
				Process getPidProcess = Runtime.getRuntime().exec("ps --ppid "+ pid +" -o pid --no-heading");
				m_log.debug("run() : getPidProcess.waitFor() : "+ getPidProcess.waitFor());
				
				InputStream is = getPidProcess.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));

				boolean getChildPid = false;
				String data = "";
				try {
					while((data = br.readLine()) != null){
						if( !data.equals("") ){
							// 起動元プロセス（sudo）でなく、その子プロセス（起動したいコマンド）にシグナル送信
							String command = "kill -15 " + data;
							m_log.info("run() : command : "+ command);
							Process sigtermProcess = Runtime.getRuntime().exec(command);
							m_log.debug("run() : sigtermProcess.waitFor() : "+ sigtermProcess.waitFor());
							// 正常終了（戻り値が0）をチェック
							if( sigtermProcess.exitValue() == 0 ){
								getChildPid = true;
							}
						}
					}
					if( !getChildPid ){
						// 子プロセスを取得できなかったので起動元を停止させる
						process.destroy();
					}
				} finally {
					is.close();
					br.close();
				}
			} else {
				process.destroy();
			}
			sendme.body.setEndValue(process.waitFor());
		} catch (RuntimeException e) {
			// findbus対応 catch (Exception e) に対して RuntimeException のキャッチを明示化
			m_log.warn("shutdown process : " + e.getMessage());
			
			// エラーを返す
			sendme.body.setTime(HinemosTime.getDateInstance().getTime());
			sendme.body.setEndValue(-1);
			sendme.body.setStatus(RunStatusConstant.ERROR);
			sendme.body.setMessage(e.getMessage());
			sendme.body.setErrorMessage("");
			m_sendQueue.put(sendme);
			return;
		} catch (Exception e) {
			m_log.warn("shutdown process : " + e.getMessage());
			
			// エラーを返す
			sendme.body.setTime(HinemosTime.getDateInstance().getTime());
			sendme.body.setEndValue(-1);
			sendme.body.setStatus(RunStatusConstant.ERROR);
			sendme.body.setMessage(e.getMessage());
			sendme.body.setErrorMessage("");
			m_sendQueue.put(sendme);
			return;
		}

		// ---------------------------
		// -- 終了メッセージ送信
		// ---------------------------

		sendme.body.setTime(HinemosTime.getDateInstance().getTime());
		sendme.body.setStatus(RunStatusConstant.END);
		m_sendQueue.put(sendme);

		////実行履歴から削除
		RunHistoryUtil.delRunHistory(m_info);

		m_log.debug("run end");
	}
}
