/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

/**
 * RPA管理ツールの Rest APIへの接続に失敗した場合に利用するException
 */
public class RpaManagementRestConnectFailed  extends HinemosException {

	private static final long serialVersionUID = -2105823431291222131L;

	/**
	 * コンストラクタ
	 */
	public RpaManagementRestConnectFailed() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public RpaManagementRestConnectFailed(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public RpaManagementRestConnectFailed(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public RpaManagementRestConnectFailed(String messages, Throwable e) {
		super(messages, e);
	}

}
