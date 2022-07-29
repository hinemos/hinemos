/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * レポートファイルが存在しない場合に利用するException
 */
public class ReportFileNotFound extends HinemosNotFound {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * ReportFileNotFoundコンストラクタ
	 */
	public ReportFileNotFound() {
		super();
	}

	/**
	 * ReportFileNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public ReportFileNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * ReportFileNotFoundコンストラクタ
	 * @param messages
	 */
	public ReportFileNotFound(String messages) {
		super(messages);
	}

	/**
	 * ReportingNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public ReportFileNotFound(Throwable e) {
		super(e);
	}
}
