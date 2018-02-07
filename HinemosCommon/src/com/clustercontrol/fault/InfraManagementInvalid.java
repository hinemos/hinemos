/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 環境構築機能で参照環境構築モジュールのネスト参照が深い場合にthrowされる
 */
public class InfraManagementInvalid extends HinemosException {

	private static final long serialVersionUID = 1L;

	/**
	 * コンストラクタ
	 */
	public InfraManagementInvalid() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public InfraManagementInvalid(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public InfraManagementInvalid(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public InfraManagementInvalid(String messages, Throwable e) {
		super(messages, e);
	}
}
