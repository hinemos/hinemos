/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.job;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.openapitools.client.model.SetJobResultRequest;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.SendQueue.JobResultSendableObject;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.util.HinemosTime;

/**
 * チェックサムを行うスレッドクラス<BR>
 * 
 */
public class CheckSumThread extends AgentThread {

	//ロガー
	private static Log m_log = LogFactory.getLog(CheckSumThread.class);

	protected static final String ALGORITHM = "MD5";

	/**
	 * コンストラクタ
	 * 
	 * @param props
	 */
	public CheckSumThread(
			AgtRunInstructionInfoResponse info,
			SendQueue sendQueue) {
		super(info, sendQueue);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		m_log.debug("run start");

		Date startDate = HinemosTime.getDateInstance();

		//実行履歴に追加
		RunHistoryUtil.addRunHistory(m_info, RunHistoryUtil.dummyProcess());

		//---------------------------
		//-- 開始メッセージ送信
		//---------------------------

		//メッセージ作成
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
		sendme.body.setTime(startDate.getTime());

		m_log.info("run SessionID=" + m_info.getSessionId() + ", JobID=" + m_info.getJobId());

		//送信
		m_sendQueue.put(sendme);

		if(m_info.getCommand().equals(CommandConstant.GET_CHECKSUM)){
			String checksum = getCheckSum(m_info.getFilePath());
			if(checksum != null){
				sendme.body.setStatus(RunStatusConstant.END);
				sendme.body.setCheckSum(checksum);
				sendme.body.setTime(HinemosTime.getDateInstance().getTime());
				sendme.body.setErrorMessage("");
				sendme.body.setMessage("");
				sendme.body.setEndValue(0);
			}
			else{
				String message = "GET_CHECKSUM is failure";
				m_log.warn(message);
				sendme.body.setStatus(RunStatusConstant.ERROR);
				sendme.body.setTime(HinemosTime.getDateInstance().getTime());
				sendme.body.setErrorMessage("");
				sendme.body.setMessage(message);
				sendme.body.setEndValue(-1);
			}
		}
		else if(m_info.getCommand().equals(CommandConstant.CHECK_CHECKSUM)){
			String checksum = getCheckSum(m_info.getFilePath());
			if(checksum.equals(m_info.getCheckSum())){
				sendme.body.setStatus(RunStatusConstant.END);
				sendme.body.setTime(HinemosTime.getDateInstance().getTime());
				sendme.body.setErrorMessage("");
				sendme.body.setMessage("");
				sendme.body.setEndValue(0);
			}
			else{
				String message = "CHECK_CHECKSUM is failure." +
						" from=" + m_info.getCheckSum() + ", to=" + checksum;
				m_log.warn(message);
				sendme.body.setStatus(RunStatusConstant.ERROR);
				sendme.body.setTime(HinemosTime.getDateInstance().getTime());
				sendme.body.setErrorMessage("");
				sendme.body.setMessage(message);
				sendme.body.setEndValue(-1);
			}
		}

		//送信
		m_sendQueue.put(sendme);

		//実行履歴から削除
		RunHistoryUtil.delRunHistory(m_info);

		m_log.debug("run end");
	}

	/**
	 * チェックサム取得
	 * 
	 * @param path
	 * @return
	 */
	private String getCheckSum(String path) {
		m_log.debug("get checksum start");

		String checksum = null;
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(path);
			MessageDigest md = MessageDigest.getInstance(ALGORITHM);

			byte[] readData = new byte[256];
			int len;
			while ((len = inputStream.read(readData)) >=0) {
				md.update(readData, 0, len);
			}
			inputStream.close();

			checksum = changeString(md.digest());

		} catch (Exception e) {
			m_log.warn("getCheckSum error. " + e.getMessage(), e);
		} finally {
			if (inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					throw new InternalError("failed to close the FileInputStream");
				}
		}

		m_log.debug("get checksum end. path=" + path + ", md5=" + checksum);
		return checksum;
	}

	/**
	 * ハッシュ値を文字列に変換する
	 * 
	 * @param digest
	 * @return
	 */
	private String changeString(byte[] digest) {
		StringBuilder hashString = new StringBuilder();
		for (int i = 0; i < digest.length; i++) {
			int d = digest[i];
			if (d < 0) {//負の値を補正
				d += 256;
			}
			if (d < 16) {//1けたは2けたする
				hashString.append("0");
			}
			hashString.append(Integer.toString(d, 16));//16進数2けたにする
		}
		return hashString.toString();
	}
}
