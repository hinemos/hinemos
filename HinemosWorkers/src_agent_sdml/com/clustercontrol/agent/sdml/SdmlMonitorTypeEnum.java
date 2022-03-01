/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.sdml;

import java.util.regex.Pattern;

public enum SdmlMonitorTypeEnum {

	// Name(SdmlMonitorTypeId, format)
	PROCESS("PRC"),
	LOG_APPLICATION("LOG_APP"),
	INTERNAL_DEADLOCK("INT_DLK"),
	INTERNAL_HEAP_REMAINING("INT_HPR"),
	INTERNAL_GC_COUNT("INT_GCC"),
	INTERNAL_CPU_USAGE("INT_CPU");

	private static final String TIMESTAMP_FORMAT = "\\d{4}-\\d{2}-\\d{2}T\\d{2}\\:\\d{2}\\:\\d{2},\\d{3}[+-]\\d{2}\\:\\d{2}";

	private final String id;

	private final Pattern format;

	private SdmlMonitorTypeEnum(final String id) {
		this.id = id;
		this.format = Pattern.compile(String.format("^%s %s .*", TIMESTAMP_FORMAT, id), Pattern.DOTALL);
	}

	public String getId() {
		return id;
	}

	public Pattern getFormat() {
		return format;
	}
	public static SdmlMonitorTypeEnum toEnum(final String id) {
		for (SdmlMonitorTypeEnum s : values()) {
			if (s.getId().equals(id)) {
				return s;
			}
		}
		return null;
	}
}
