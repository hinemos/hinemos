/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.log.control;

public enum ControlCode {
	INITIALIZE_BEGIN("Initialize_Begin"),
	INITIALIZE_SET("Initialize_Set"),
	INITIALIZE_END("Initialize_End"),
	START("Start"),
	STOP("Stop"),
	ERROR("Error"),
	WARNING("Warning"),
	INFORM("Info");

	private final String msg;

	private ControlCode(final String msg) {
		this.msg = msg;
	}

	public String getString() {
		return this.msg;
	}
}
