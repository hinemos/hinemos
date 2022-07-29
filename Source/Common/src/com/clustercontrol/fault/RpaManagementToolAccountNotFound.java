/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPAスコープが存在しない場合に利用するException
 */
public class RpaManagementToolAccountNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 1L;
	
	public RpaManagementToolAccountNotFound() {
		super();
	}
	
	public RpaManagementToolAccountNotFound(String messages) {
		super(messages);
	}
	
	public RpaManagementToolAccountNotFound(Throwable e) {
		super(e);
	}

	public RpaManagementToolAccountNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
