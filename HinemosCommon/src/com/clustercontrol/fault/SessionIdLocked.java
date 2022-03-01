/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

/**
 * セッションIDのロック取得中の場合に利用するException
 * @version 7.0.0
 */
public class SessionIdLocked extends HinemosException  {

	private static final long serialVersionUID = -3781094707245519945L;
	private String m_jobSessionId = null;

	/**
	 * SessionIdLockedExceptionコンストラクタ
	 */
	public SessionIdLocked() {
		super();
	}

	/**
	 * SessionIdLockedExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public SessionIdLocked(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * SessionIdLockedExceptionコンストラクタ
	 * @param messages
	 */
	public SessionIdLocked(String messages) {
		super(messages);
	}

	/**
	 * SessionIdLockedExceptionコンストラクタ
	 * @param e
	 */
	public SessionIdLocked(Throwable e) {
		super(e);
	}

	public String getJobSessionId() {
		return m_jobSessionId;
	}

	public void setJobSessionId(String jobSessionId) {
		m_jobSessionId = jobSessionId;
	}

}
