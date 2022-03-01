/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory;

/**
 * RPAログの解析に失敗した際に利用するException<BR>
 * RpaLogParserでパースに失敗した場合、本例外をthrowすること。
 * @see RpaLogParser#parse
 *
 */
public class RpaLogParseException extends Exception {

	private static final long serialVersionUID = 1L;

	public RpaLogParseException() {
		super();
	}

	public RpaLogParseException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RpaLogParseException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RpaLogParseException(String message) {
		super(message);
	}

	public RpaLogParseException(Throwable cause) {
		super(cause);
	}

}
