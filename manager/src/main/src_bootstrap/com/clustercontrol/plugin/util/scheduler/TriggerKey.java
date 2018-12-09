/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util.scheduler;

public final class TriggerKey extends Key<TriggerKey> {

	private static final long serialVersionUID = 8070357886703449660L;

	public TriggerKey(String name) {
		super(name, null);
	}

	public TriggerKey(String name, String group) {
		super(name, group);
	}

	public static TriggerKey triggerKey(String name) {
		return new TriggerKey(name, null);
	}

	public static TriggerKey triggerKey(String name, String group) {
		return new TriggerKey(name, group);
	}

}
