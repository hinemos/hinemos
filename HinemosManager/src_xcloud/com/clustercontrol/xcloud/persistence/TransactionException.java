/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.persistence;

public class TransactionException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8491882553267194446L;

	public TransactionException() {
		super();
	}

	public TransactionException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TransactionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TransactionException(String message) {
		super(message);
	}

	public TransactionException(Throwable cause) {
		super(cause);
	}
}
