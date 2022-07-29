/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPA管理ツール停止方法マスタが存在しない場合に利用するException
 */
public class RpaManagementToolStopModeMasterNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 1L;

	public RpaManagementToolStopModeMasterNotFound() {
		super();
	}

	public RpaManagementToolStopModeMasterNotFound(String messages) {
		super(messages);
	}

	public RpaManagementToolStopModeMasterNotFound(Throwable e) {
		super(e);
	}

	public RpaManagementToolStopModeMasterNotFound(String messages, Throwable e) {
		super(messages, e);
	}

}
