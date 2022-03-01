/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.util;

public enum SeparationType {
	HEAD_PATTERN(1), TAIL_PATTTERN(2), FILE_RETURNCODE(3);

	private final Integer i;

	private SeparationType(final Integer i) {
		this.i = i;
	}

	public static Integer stringToInt(String str) {
		switch (str) {
		case "HeadPattern":
			return HEAD_PATTERN.getInt();
		case "TailPattern":
			return TAIL_PATTTERN.getInt();
		case "FileReturnCode":
			return FILE_RETURNCODE.getInt();
		default:
			return null;
		}
	}

	private Integer getInt() {
		return this.i;
	}
}
