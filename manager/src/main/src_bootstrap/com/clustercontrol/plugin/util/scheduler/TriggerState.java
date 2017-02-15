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