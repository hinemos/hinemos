/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.util;

public enum PriorityType {
	INFO(3), WARN(2), UNKNOWN(1), CRITICAL(0),;
	private final Integer i;

	private PriorityType(final Integer i) {
		this.i = i;
	}

	public static Integer stringToInt(String str) {
		if (str == null) {
			return null;
		}
		switch (str) {
		case "info":
			return INFO.getInt();
		case "warning":
			return WARN.getInt();
		case "unknown":
			return UNKNOWN.getInt();
		case "critical":
			return CRITICAL.getInt();
		default:
			return null;
		}
	}

	private Integer getInt() {
		return this.i;
	}
}
