/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * レポーティングが存在しない場合に利用するException
 * @version 4.1.2
 */
public class ReportingNotFound extends HinemosNotFound {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String m_reportingId = null;

	/**
	 * ReportingNotFoundExceptionコンストラクタ
	 */
	public ReportingNotFound() {
		super();
	}

	/**
	 * ReportingNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public ReportingNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * ReportingNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public ReportingNotFound(String messages) {
		super(messages);
	}

	/**
	 * ReportingNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public ReportingNotFound(Throwable e) {
		super(e);
	}

	/**
	 * レポーティングIDを返します。
	 * @return レポーティングID
	 */
	public String getReportingId() {
		return m_reportingId;
	}

	/**
	 * レポーティングIDを設定します。
	 * @param reportingId レポーティングID
	 */
	public void setReportingId(String reportingId) {
		m_reportingId = reportingId;
	}
}
