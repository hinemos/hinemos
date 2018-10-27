/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 適切なroleを持っていない場合に利用するException
 * @version 3.2.0
 */
public class InvalidRole extends HinemosException {

	private static final long serialVersionUID = 1636867491172325635L;

	/**
	 * コンストラクタ
	 */
	public InvalidRole() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public InvalidRole(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public InvalidRole(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public InvalidRole(String messages, Throwable e) {
		super(messages, e);
	}
}
