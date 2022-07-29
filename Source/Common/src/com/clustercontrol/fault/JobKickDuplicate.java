/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * jobKicKIDが重複している場合に利用するException
 * @version 4.1.0
 */
public class JobKickDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = -3093747190605885555L;
	private String m_jobKickId = null;

	/**
	 * JobKickDuplicateExceptionコンストラクタ
	 */
	public JobKickDuplicate() {
		super();
	}

	/**
	 * JobKickDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public JobKickDuplicate(String messages) {
		super(messages);
	}

	/**
	 * JobKickDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public JobKickDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * JobKickDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public JobKickDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getJobKickId() {
		return m_jobKickId;
	}

	public void setJobKIckId(String jobKickId) {
		m_jobKickId = jobKickId;
	}
}
