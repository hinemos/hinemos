/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ユーザとパスワードの組み合わせが間違っている場合に利用するException
 * @version 3.2.0
 */
public class InvalidUserPass extends HinemosInvalid {

	private static final long serialVersionUID = -6401349857743376125L;

	/**
	 * コンストラクタ
	 */
	public InvalidUserPass() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public InvalidUserPass(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public InvalidUserPass(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public InvalidUserPass(String messages, Throwable e) {
		super(messages, e);
	}
}
