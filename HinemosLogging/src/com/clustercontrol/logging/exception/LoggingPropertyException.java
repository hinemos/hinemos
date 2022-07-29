/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logging.exception;

public class LoggingPropertyException extends Exception {

	private static final long serialVersionUID = 1L;

	public LoggingPropertyException() {
		super();
	}

	public LoggingPropertyException(String message) {
		super(message);
	}

	public LoggingPropertyException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoggingPropertyException(Throwable cause) {
		super(cause);
	}
}
