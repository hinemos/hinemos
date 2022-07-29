/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPA管理ツールマスタが存在しない場合に利用するException
 */
public class RpaManagementToolMasterNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 1L;
	
	public RpaManagementToolMasterNotFound() {
		super();
	}
	
	public RpaManagementToolMasterNotFound(String messages) {
		super(messages);
	}
	
	public RpaManagementToolMasterNotFound(Throwable e) {
		super(e);
	}

	public RpaManagementToolMasterNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
