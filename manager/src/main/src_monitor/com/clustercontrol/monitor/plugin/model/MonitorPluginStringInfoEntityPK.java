/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.plugin.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The primary key class for the cc_monitor_plugin_string_info database table.
 * 
 */
@Embeddable
public class MonitorPluginStringInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private String key;

	public MonitorPluginStringInfoEntityPK() {
	}

	public MonitorPluginStringInfoEntityPK(String monitorId, String key) {
		this.monitorId = monitorId;
		this.key = key;
	}


	@Column(name="monitor_id")
	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="property_key")
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorPluginStringInfoEntityPK)) {
			return false;
		}
		MonitorPluginStringInfoEntityPK castOther = (MonitorPluginStringInfoEntityPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.key.equals(castOther.key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.key.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"key"
		};
		String[] values = {
				this.monitorId,
				this.key
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}
