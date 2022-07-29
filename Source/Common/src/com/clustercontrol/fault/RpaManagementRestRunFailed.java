/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

public class RpaManagementRestRunFailed extends HinemosException {

	private static final long serialVersionUID = 1L;

	/**
	 * コンストラクタ
	 */
	public RpaManagementRestRunFailed() {
		super();
	}

	/**
	 * コンストラクタ
	 * 
	 * @param messages
	 */
	public RpaManagementRestRunFailed(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param e
	 */
	public RpaManagementRestRunFailed(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param messages
	 * @param e
	 */
	public RpaManagementRestRunFailed(String messages, Throwable e) {
		super(messages, e);
	}
}
