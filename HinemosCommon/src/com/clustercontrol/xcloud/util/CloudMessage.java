/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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
		StringBuilder ret = new StringBuilder(PREFIX);
		ret.append(key);
		for (String arg : args) {
			ret.append(DELIMITER).append(ARGS_SEPARATOR).append(arg).append(ARGS_SEPARATOR);
		}
		return ret.append(POSTFIX).toString();
	}
	
	public static String escape (String s) {
		return s.replaceAll("$\\[", ESCAPE).replaceAll("\\]", ESCAPE).replaceAll("\\:", ESCAPE);
	}
}
