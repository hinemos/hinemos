/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

public class InfraModuleNotFound extends HinemosNotFound {
	
	private static final long serialVersionUID = 1L;

	/**
	 * InfraCheckResultNotFound コンストラクタ
	 * @param messages
	 * @param e
	 */
	public InfraModuleNotFound(String messages) {
		super(messages);
	}
}
