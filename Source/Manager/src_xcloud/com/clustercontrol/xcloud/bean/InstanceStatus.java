/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

public enum InstanceStatus {
	running("running"),
	processing("processing"),
	terminated("terminated"),
	stopped("stopped"),
	unknown("unknown"),
	suspend("suspend"),
	missing("missing");

	private final String label;

	private InstanceStatus(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	public static InstanceStatus byLabel(String label) {
		String name = label.replace('-', '_');
		return valueOf(name);
	}
}
