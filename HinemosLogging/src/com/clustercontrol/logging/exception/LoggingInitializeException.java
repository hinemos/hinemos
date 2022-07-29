/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logging.exception;

public class LoggingInitializeException extends Exception {

	private static final long serialVersionUID = 1L;

	public LoggingInitializeException() {
		super();
	}

	public LoggingInitializeException(String message) {
		super(message);
	}

	public LoggingInitializeException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoggingInitializeException(Throwable cause) {
		super(cause);
	}
}
