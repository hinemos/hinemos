/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.util;

public enum SdmlControlStatusEnum {

	Waiting(0),
	Initializing(1),
	BeforeStart(2),
	Monitoring(3);
	// 監視設定の有無は内部的に同じ扱いなのでステータスは定義しない
	//Stopped(4),
	//Updating(5);

	private final Integer value;

	private SdmlControlStatusEnum(final Integer value) {
		this.value = value;
	}

	public Integer getValue() {
		return value;
	}

	public static SdmlControlStatusEnum valueOf(final Integer intValue) {
		for (SdmlControlStatusEnum s : values()) {
			if (s.getValue().equals(intValue)) {
				return s;
			}
		}
		return null;
	}
}
