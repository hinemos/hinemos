/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

public class InvalidStateException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7086307121983459290L;

	public InvalidStateException() {
		super();
	}

	public InvalidStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidStateException(String message) {
		super(message);
	}

	public InvalidStateException(Throwable cause) {
		super(cause);
	}
}
