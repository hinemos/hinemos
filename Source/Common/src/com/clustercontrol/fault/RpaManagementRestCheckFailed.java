/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

public class RpaManagementRestCheckFailed extends HinemosException {

	private static final long serialVersionUID = 1L;

	/**
	 * コンストラクタ
	 */
	public RpaManagementRestCheckFailed() {
		super();
	}

	/**
	 * コンストラクタ
	 * 
	 * @param messages
	 */
	public RpaManagementRestCheckFailed(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param e
	 */
	public RpaManagementRestCheckFailed(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param messages
	 * @param e
	 */
	public RpaManagementRestCheckFailed(String messages, Throwable e) {
		super(messages, e);
	}
}
