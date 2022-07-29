/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logging.exception;

public class MonitoringFailureException extends Exception {

	private static final long serialVersionUID = 1L;

	public MonitoringFailureException() {
		super();
	}

	public MonitoringFailureException(String message) {
		super(message);
	}

	public MonitoringFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
