/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.port.protocol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.port.bean.PortRunCountConstant;
import com.clustercontrol.port.bean.PortRunIntervalConstant;
import com.clustercontrol.util.MessageConstant;

public abstract class ReachAddressProtocol {

	private static Log m_log = LogFactory.getLog(ReachAddressProtocol.class);

	/** ポート番号 */
	protected int m_portNo;

	/** 試行回数 */
	protected int m_sentCount = PortRunCountConstant.TYPE_COUNT_01;

	/** 試行間隔（ミリ秒） */
	protected int m_sentInterval = PortRunIntervalConstant.TYPE_SEC_01;

	/** タイムアウト（ミリ秒） */
	protected int m_timeout = 1000;

	/** メッセージ */
	protected String m_message = null;

	/** オリジナルメッセージ */
	protected String m_messageOrg = null;

	/** 接続を確立するまでにかかった時間（ms） */
	protected long m_response;

	/**
	 * アドレスを取得し、到達可能かどうかをテストします
	 * 
	 * @param info
	 * @return PORT監視ステータス
	 */
	public boolean isReachable(String ipNetworkNumber, String nodeName) {

		String addressText = null;
		if (ipNetworkNumber != null && !"".equals(ipNetworkNumber)) {
			addressText = ipNetworkNumber;
		} else if (nodeName != null && !"".equals(nodeName)) {
			addressText = nodeName;
		} else {
			m_log.debug("isReachable(): "
					+ MessageConstant.MESSAGE_NOT_REGISTER_IN_REPOSITORY_PORT.getMessage());
			m_message = MessageConstant.MESSAGE_NOT_REGISTER_IN_REPOSITORY_PORT.getMessage();
			m_messageOrg = null;
			return false;
		}

		boolean result = this.isRunning(addressText);
		return result;
	}

	protected abstract boolean isRunning(String addresstext);

	public String getMessage() {
		return m_message;
	}

	public void setMessage(String m_message) {
		this.m_message = m_message;
	}

	public String getMessageOrg() {
		return m_messageOrg;
	}

	public void setMessageOrg(String org) {
		m_messageOrg = org;
	}

	public int getPortNo() {
		return m_portNo;
	}

	public void setPortNo(int no) {
		m_portNo = no;
	}

	public long getResponse() {
		return m_response;
	}

	public void setResponse(long m_response) {
		this.m_response = m_response;
	}

	public int getSentCount() {
		return m_sentCount;
	}

	public void setSentCount(int count) {
		m_sentCount = count;
	}

	public int getSentInterval() {
		return m_sentInterval;
	}

	public void setSentInterval(int interval) {
		m_sentInterval = interval;
	}

	public int getTimeout() {
		return m_timeout;
	}

	public void setTimeout(int m_timeout) {
		this.m_timeout = m_timeout;
	}

}
