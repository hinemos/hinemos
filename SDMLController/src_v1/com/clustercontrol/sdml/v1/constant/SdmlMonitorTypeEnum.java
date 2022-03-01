/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.constant;

public enum SdmlMonitorTypeEnum {

	// Name(SdmlMonitorTypeId)
	PROCESS("PRC"),
	LOG_APPLICATION("LOG_APP"),
	INTERNAL_DEADLOCK("INT_DLK"),
	INTERNAL_HEAP_REMAINING("INT_HPR"),
	INTERNAL_GC_COUNT("INT_GCC"),
	INTERNAL_CPU_USAGE("INT_CPU");

	private final String id;

	private SdmlMonitorTypeEnum(final String id) {
		this.id = id;
	}

	public String getId() {
		return id;
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
