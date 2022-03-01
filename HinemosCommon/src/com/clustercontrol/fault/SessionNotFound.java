/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 環境構築機能で、セッションが見つからない場合にthrowされる
 */
public class SessionNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -9189072636555316265L;

	/**
	 * コンストラクタ
	 */
	public SessionNotFound() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public SessionNotFound(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public SessionNotFound(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public SessionNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
