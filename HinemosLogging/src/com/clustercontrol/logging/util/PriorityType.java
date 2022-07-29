/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.util;

public enum PriorityType {
	// @formatter:off
	INFO("info", 3),
	WARNING("warning", 2),
	UNKNOWN("unknown", 1),
	CRITICAL("critical", 0);
	// @formatter:on

	private final String str;
	private final Integer i;

	private PriorityType(final String str, final Integer i) {
		this.str = str;
		this.i = i;
	}

	public static Integer stringToInt(String str) {
		if (str == null) {
			return null;
		}
		for (PriorityType value : values()) {
			if (str.equals(value.getStringValue())) {
				return value.getIntValue();
			}
		}
		return null;
	}

	public static String[] getStringValues() {
		String[] strValues = new String[values().length];
		for (int i = 0; i < strValues.length; i++) {
			strValues[i] = values()[i].getStringValue();
		}
		return strValues;
	}

	public String getStringValue() {
		return str;
	}

	public Integer getIntValue() {
		return i;
	}
}
