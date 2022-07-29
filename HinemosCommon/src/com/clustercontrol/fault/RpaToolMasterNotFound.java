/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPAツールマスタが存在しない場合に利用するException
 */
public class RpaToolMasterNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 1L;
	
	public RpaToolMasterNotFound() {
		super();
	}
	
	public RpaToolMasterNotFound(String messages) {
		super(messages);
	}
	
	public RpaToolMasterNotFound(Throwable e) {
		super(e);
	}

	public RpaToolMasterNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
