/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * MonitorIdが不正な場合に利用するException
 */
public class MonitorIdInvalid extends HinemosInvalid {

	private static final long serialVersionUID = -3327297006368408032L;

	/**
	 * MonitorIdInvalidコンストラクタ
	 */
	public MonitorIdInvalid() {
		super();
	}

	/**
	 * MonitorIdInvalidコンストラクタ
	 * @param messages
	 */
	public MonitorIdInvalid(String messages) {
		super(messages);
	}

	/**
	 * MonitorIdInvalidコンストラクタ
	 * @param e
	 */
	public MonitorIdInvalid(Throwable e) {
		super(e);
	}

	/**
	 * MonitorIdInvalidコンストラクタ
	 * @param messages
	 * @param e
	 */
	public MonitorIdInvalid(String messages, Throwable e) {
		super(messages, e);
	}

}
