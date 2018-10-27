/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 収集データのPKが存在しない場合に利用するException
 * @version 5.1.0
 */
public class CollectKeyNotFound extends HinemosException {
	private static final long serialVersionUID = 3038092388339914953L;

	/**
	 * CollectKeyNotFoundコンストラクタ
	 */
	public CollectKeyNotFound() {
		super();
	}

	/**
	 * CollectKeyNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public CollectKeyNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * CollectKeyNotFoundコンストラクタ
	 * @param messages
	 */
	public CollectKeyNotFound(String messages) {
		super(messages);
	}

	/**
	 * CollectKeyNotFoundコンストラクタ
	 * @param e
	 */
	public CollectKeyNotFound(Throwable e) {
		super(e);
	}

}
