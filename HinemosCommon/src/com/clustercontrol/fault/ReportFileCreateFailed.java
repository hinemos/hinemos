/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * レポートファイルの生成失敗時に利用するException
 */
public class ReportFileCreateFailed extends HinemosNotFound {

	private static final long serialVersionUID = 1300450541890103758L;

	/**
	 * ReportFileCreateFailedコンストラクタ
	 */
	public ReportFileCreateFailed() {
		super();
	}

	/**
	 * ReportFileCreateFailedコンストラクタ
	 * @param messages
	 */
	public ReportFileCreateFailed(String messages) {
		super(messages);
	}

	/**
	 * ReportFileCreateFailedコンストラクタ
	 * @param e
	 */
	public ReportFileCreateFailed(Throwable e) {
		super(e);
	}

	/**
	 * ReportFileCreateFailedコンストラクタ
	 * @param messages
	 * @param e
	 */
	public ReportFileCreateFailed(String messages, Throwable e) {
		super(messages, e);
	}
}
