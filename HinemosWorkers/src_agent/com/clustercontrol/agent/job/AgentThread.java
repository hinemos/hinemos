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
import org.openapitools.client.model.AgtRunInstructionInfoResponse;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.util.AgentProperties;

/**
 * エージェントスレッドクラス<BR>
 * 
 * エージェントでは複数の処理に対応するスレッドクラスがあります。
 * その共通の親クラスです。
 *
 */
public abstract class AgentThread extends Thread {
	private static Log m_log = LogFactory.getLog(AgentThread.class);

	protected AgtRunInstructionInfoResponse m_info = null;
	protected SendQueue m_sendQueue = null;

	//オリジナルメッセージのサイズ上限（Byte）
	protected int m_limit_jobmsg = 1024;

	/**
	 * コンストラクタ
	 */
	public AgentThread() {

	}

	/**
	 * コンストラクタ
	 * 
	 * @param info
	 * @param sendQueue
	 * @param runHistory
	 * @param props
	 */
	public AgentThread(AgtRunInstructionInfoResponse info, SendQueue sendQueue) {
		m_info = info;
		m_sendQueue = sendQueue;

		//メッセージサイズ上限を取得
		String limit_jobmsg = AgentProperties.getProperty("job.message.length");
		if (limit_jobmsg != null) {
			try {
				m_limit_jobmsg = Integer.parseInt(limit_jobmsg);
				m_log.info("job.message.length = " + m_limit_jobmsg + " byte");
			} catch (NumberFormatException e) {
				m_log.error("job.message.length",e);
			}
		}
	}
}
