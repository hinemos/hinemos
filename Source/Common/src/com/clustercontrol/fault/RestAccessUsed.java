/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

public class RestAccessUsed extends HinemosUsed {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7500064523097671879L;

	/**
	 * RestAccessUsedコンストラクタ
	 */
	public RestAccessUsed() {
		super();
	}

	/**
	 * RestAccessUsedコンストラクタ
	 * @param messages
	 */
	public RestAccessUsed(String messages) {
		super(messages);
	}

	/**
	 * RestAccessUsedコンストラクタ
	 * @param e
	 */
	public RestAccessUsed(Throwable e) {
		super(e);
	}

	/**
	 * RestAccessUsedコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RestAccessUsed(String messages, Throwable e) {
		super(messages, e);
	}
}
