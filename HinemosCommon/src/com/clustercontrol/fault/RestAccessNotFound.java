/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

public class RestAccessNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 569519677148002532L;

	/**
	 * RestAccessNotFoundコンストラクタ
	 */
	public RestAccessNotFound() {
		super();
	}

	/**
	 * RestAccessNotFoundコンストラクタ
	 * @param messages
	 */
	public RestAccessNotFound(String messages) {
		super(messages);
	}

	/**
	 * RestAccessNotFoundコンストラクタ
	 * @param e
	 */
	public RestAccessNotFound(Throwable e) {
		super(e);
	}

	/**
	 * RestAccessNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RestAccessNotFound(String messages, Throwable e) {
		super(messages, e);
	}

}
