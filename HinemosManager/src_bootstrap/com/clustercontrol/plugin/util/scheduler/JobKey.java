/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util.scheduler;


public class JobKey extends Key<JobKey> {
	private static final long serialVersionUID = -6073883950062574010L;

	public JobKey(String name) {
		super(name, null);
	}

	public JobKey(String name, String group) {
		super(name, group);
	}

	public static JobKey jobKey(String name) {
		return new JobKey(name, null);
	}

	public static JobKey jobKey(String name, String group) {
		return new JobKey(name, group);
	}
}