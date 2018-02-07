/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 適切なタイムゾーンではない場合に利用するException
 * @version 5.1.0
 */
public class InvalidTimezone extends HinemosException {

	private static final long serialVersionUID = 1636867491172325635L;

	/**
	 * コンストラクタ
	 */
	public InvalidTimezone() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public InvalidTimezone(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public InvalidTimezone(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public InvalidTimezone(String messages, Throwable e) {
		super(messages, e);
	}
}