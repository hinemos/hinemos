/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * Hinemos独自のErrorの場合に利用するException
 * @version 3.2.0
 */
public class SnmpResponseError extends HinemosException {

	private static final long serialVersionUID = 6157131613222270776L;

	/**
	 * HinemosUnknownコンストラクタ
	 */
	public SnmpResponseError() {
		super();
	}

	/**
	 * HinemosUnknownコンストラクタ
	 * @param messages
	 */
	public SnmpResponseError(String messages) {
		super(messages);
	}

	/**
	 * HinemosUnknownコンストラクタ
	 * @param e
	 */
	public SnmpResponseError(Throwable e) {
		super(e);
	}

	/**
	 * HinemosUnknownコンストラクタ
	 * @param messages
	 * @param e
	 */
	public SnmpResponseError(String messages, Throwable e) {
		super(messages, e);
	}
}
