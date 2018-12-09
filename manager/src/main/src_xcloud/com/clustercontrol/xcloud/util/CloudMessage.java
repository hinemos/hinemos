/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

public class CloudMessage {
	
	private static final String DELIMITER = ":";
	private static final String ARGS_SEPARATOR = "\"";
	private static final String PREFIX = "$[";
	private static final String POSTFIX = "]";
	private static final String ESCAPE = ".";
	
	public static String getMessage(String key, String... args) {
		String ret = PREFIX + key;

		for (String arg : args) {
			ret += DELIMITER + ARGS_SEPARATOR + arg + ARGS_SEPARATOR;
		}
		return ret + POSTFIX;
	}
	
	public static String escape (String s) {
		return s.replaceAll("$\\[", ESCAPE).replaceAll("\\]", ESCAPE).replaceAll("\\:", ESCAPE);
	}
}
