/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPA管理ツール起動パラメータマスタが存在しない場合に利用するException
 */
public class RpaManagementToolRunParamMasterNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 1L;

	public RpaManagementToolRunParamMasterNotFound() {
		super();
	}

	public RpaManagementToolRunParamMasterNotFound(String messages) {
		super(messages);
	}

	public RpaManagementToolRunParamMasterNotFound(Throwable e) {
		super(e);
	}

	public RpaManagementToolRunParamMasterNotFound(String messages, Throwable e) {
		super(messages, e);
	}

}
