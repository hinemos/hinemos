/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 収集ログ等のDB検索時のタイムアウトエラーが発生した場合に利用するException
 * @version 6.1.0
 */
public class HinemosDbTimeout extends HinemosException {

	private static final long serialVersionUID = 1L;

	/**
	 * HinemosDbTimeoutコンストラクタ
	 */
	public HinemosDbTimeout() {
		super();
	}

	/**
	 * HinemosTimeoutExceptionコンストラクタ
	 * @param messages
	 */
	public HinemosDbTimeout(String messages) {
		super(messages);
	}

	/**
	 * HinemosTimeoutExceptionコンストラクタ
	 * @param e
	 */
	public HinemosDbTimeout(Throwable e) {
		super(e);
	}

	/**
	 * HinemosTimeoutExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public HinemosDbTimeout(String messages, Throwable e) {
		super(messages, e);
	}
}
