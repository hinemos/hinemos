/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

/**
 * Rest(Web-Api)への接続に失敗した場合に利用するException
 * @version 3.2.0
 */
public class RestConnectFailed  extends HinemosException {

	private static final long serialVersionUID = -2105823431291222131L;

	/**
	 * コンストラクタ
	 */
	public RestConnectFailed() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public RestConnectFailed(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public RestConnectFailed(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public RestConnectFailed(String messages, Throwable e) {
		super(messages, e);
	}

}
