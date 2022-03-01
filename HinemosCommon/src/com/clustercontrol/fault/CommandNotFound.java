/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * facilityIDが存在しない場合に利用するException
 * @version 3.2.0
 */
public class CommandNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -435935118433877282L;

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 */
	public CommandNotFound() {
		super();
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public CommandNotFound(String messages) {
		super(messages);
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public CommandNotFound(Throwable e) {
		super(e);
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public CommandNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
