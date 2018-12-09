/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * EventLogが存在しない場合に利用するException
 * @version 3.2.0
 */
public class EventLogNotFound extends HinemosException {

	private static final long serialVersionUID = 4418365043337486952L;

	/**
	 * コンストラクタ
	 */
	public EventLogNotFound() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public EventLogNotFound(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public EventLogNotFound(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public EventLogNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
