/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPA管理ツール実行種別マスタが存在しない場合に利用するException
 */
public class RpaManagementToolRunTypeMasterNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 1L;

	public RpaManagementToolRunTypeMasterNotFound() {
		super();
	}

	public RpaManagementToolRunTypeMasterNotFound(String messages) {
		super(messages);
	}

	public RpaManagementToolRunTypeMasterNotFound(Throwable e) {
		super(e);
	}

	public RpaManagementToolRunTypeMasterNotFound(String messages, Throwable e) {
		super(messages, e);
	}

}
