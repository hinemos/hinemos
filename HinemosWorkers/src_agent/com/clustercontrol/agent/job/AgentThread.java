/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.agent.job;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.ws.jobmanagement.RunInstructionInfo;

/**
 * エージェントスレッドクラス<BR>
 * 
 * エージェントでは複数の処理に対応するスレッドクラスがあります。
 * その共通の親クラスです。
 *
 */
public abstract class AgentThread extends Thread {
	private static Log m_log = LogFactory.getLog(AgentThread.class);

	protected RunInstructionInfo m_info = null;
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
	public AgentThread(RunInstructionInfo info, SendQueue sendQueue) {
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
