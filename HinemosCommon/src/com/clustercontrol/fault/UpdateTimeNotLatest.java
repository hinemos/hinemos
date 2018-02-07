/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

public class UpdateTimeNotLatest extends HinemosException {

	private static final long serialVersionUID = -1247901052275928841L;

	/**
	 * コンストラクタ
	 */
	public UpdateTimeNotLatest() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public UpdateTimeNotLatest(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public UpdateTimeNotLatest(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public UpdateTimeNotLatest(String messages, Throwable e) {
		super(messages, e);
	}
}
