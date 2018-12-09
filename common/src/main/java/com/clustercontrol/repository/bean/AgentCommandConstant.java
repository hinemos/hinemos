/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

public class AgentCommandConstant {

	public static final int RESTART = 1;
	public static final int UPDATE = 2;

	private AgentCommandConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
