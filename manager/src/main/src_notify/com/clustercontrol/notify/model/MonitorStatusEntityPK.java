/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_monitor_status database table.
 * 
 */
@Embeddable
public class MonitorStatusEntityPK implements Serializable, Comparable<MonitorStatusEntityPK> {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private String pluginId;
	private String monitorId;
	private String subKey;

	public MonitorStatusEntityPK() {
	}

	public MonitorStatusEntityPK(String facilityId, String pluginId, String monitorId, String subKey) {
		this.setFacilityId(facilityId);
		this.setPluginId(pluginId);
		this.setMonitorId(monitorId);
		this.setSubKey(subKey);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="plugin_id")
	public String getPluginId() {
		return this.pluginId;
	}
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="sub_key")
	public String getSubKey() {
		return this.subKey;
	}
	public void setSubKey(String subKey) {
		this.subKey = subKey;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorStatusEntityPK)) {
			return false;
		}
		MonitorStatusEntityPK castOther = (MonitorStatusEntityPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.pluginId.equals(castOther.pluginId)
				&& this.monitorId.equals(castOther.monitorId)
				&& this.subKey.equals(castOther.subKey);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.pluginId.hashCode();
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.subKey.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"pluginId",
				"monitorId",
				"subKey"
		};
		String[] values = {
				this.facilityId,
				this.pluginId,
				this.monitorId,
				this.subKey
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}

	@Override
	public int compareTo( MonitorStatusEntityPK o ){
		int ret;
		
		ret = facilityId.compareTo( o.facilityId );
		if( ret != 0 ){
			return ret;
		}
		ret = pluginId.compareTo( o.pluginId );
		if( ret != 0 ){
			return ret;
		}
		ret = monitorId.compareTo( o.monitorId );
		if( ret != 0 ){
			return ret;
		}
		ret = subKey.compareTo( o.subKey );
		if( ret != 0 ){
			return ret;
		}
		return 0;
	}
}