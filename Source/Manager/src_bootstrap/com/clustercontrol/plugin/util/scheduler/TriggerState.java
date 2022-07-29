/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util.scheduler;

public enum TriggerState {
	VIRGIN,
	SCHEDULED,
	PENDING,
	EXECUTING,
	EXECUTED,
	CANCELLED,
	PAUSED,
	ERROR;
}