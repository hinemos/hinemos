/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * Collectorが存在しない場合に利用するException
 * @version 3.2.0
 */
public class CollectorNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 8373432999993350125L;

	/**
	 * CollectorNotFoundコンストラクタ
	 */
	public CollectorNotFound() {
		super();
	}

	/**
	 * CollectorNotFoundコンストラクタ
	 * @param messages
	 */
	public CollectorNotFound(String messages) {
		super(messages);
	}

	/**
	 * CollectorNotFoundコンストラクタ
	 * @param e
	 */
	public CollectorNotFound(Throwable e) {
		super(e);
	}

	/**
	 * CollectorNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public CollectorNotFound(String messages, Throwable e) {
		super(messages, e);
	}

}
