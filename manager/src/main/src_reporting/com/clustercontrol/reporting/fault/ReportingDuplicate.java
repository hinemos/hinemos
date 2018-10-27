/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.fault;

import com.clustercontrol.fault.HinemosException;

/**
 * reportingIDが重複している場合に利用するException
 * @version 4.1.2
 */
public class ReportingDuplicate extends HinemosException {

	private static final long serialVersionUID = 1L;
	private String m_reportingId = null;

	/**
	 * ReportingDuplicateコンストラクタ
	 */
	public ReportingDuplicate() {
		super();
	}

	/**
	 * ReportingDuplicateコンストラクタ
	 * @param messages
	 */
	public ReportingDuplicate(String messages) {
		super(messages);
	}

	/**
	 * ReportingDuplicateコンストラクタ
	 * @param e
	 */
	public ReportingDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * ReportingDuplicateコンストラクタ
	 * @param messages
	 * @param e
	 */
	public ReportingDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getReportingId() {
		return m_reportingId;
	}

	public void setReportingId(String reportingId) {
		m_reportingId = reportingId;
	}
}
