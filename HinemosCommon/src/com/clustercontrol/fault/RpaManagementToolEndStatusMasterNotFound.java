/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPA管理ツール終了状態マスタが存在しない場合に利用するException
 */
public class RpaManagementToolEndStatusMasterNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 1L;

	public RpaManagementToolEndStatusMasterNotFound() {
		super();
	}

	public RpaManagementToolEndStatusMasterNotFound(String messages) {
		super(messages);
	}

	public RpaManagementToolEndStatusMasterNotFound(Throwable e) {
		super(e);
	}

	public RpaManagementToolEndStatusMasterNotFound(String messages, Throwable e) {
		super(messages, e);
	}

}
