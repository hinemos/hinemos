/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * JobSessionIDが重複している場合に利用するException
 * @version 4.1.0
 */
public class JobSessionDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = -3292533279221432680L;
	private String m_jobSessionId = null;

	/**
	 * JobSessionDuplicateExceptionコンストラクタ
	 */
	public JobSessionDuplicate() {
		super();
	}

	/**
	 * JobSessionDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public JobSessionDuplicate(String messages) {
		super(messages);
	}

	/**
	 * JobSessionDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public JobSessionDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * JobSessionDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public JobSessionDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getJobSessionId() {
		return m_jobSessionId;
	}

	public void setJobSessionId(String jobSessionId) {
		m_jobSessionId = jobSessionId;
	}
}
