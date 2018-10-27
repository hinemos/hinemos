/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.monitor.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_status_info database table.
 * 
 */
@Embeddable
public class StatusInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private String monitorId;
	private String monitorDetailId;
	private String pluginId;

	public StatusInfoEntityPK() {
	}

	public StatusInfoEntityPK(String facilityId,
			String monitorId,
			String monitorDetailId,
			String pluginId) {
		this.setFacilityId(facilityId);
		this.setMonitorId(monitorId);
		this.setMonitorDetailId(monitorDetailId);
		this.setPluginId(pluginId);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="monitor_detail_id")
	public String getMonitorDetailId() {
		return this.monitorDetailId;
	}
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	@Column(name="plugin_id")
	public String getPluginId() {
		return this.pluginId;
	}
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof StatusInfoEntityPK)) {
			return false;
		}
		StatusInfoEntityPK castOther = (StatusInfoEntityPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.monitorId.equals(castOther.monitorId)
				&& this.monitorDetailId.equals(castOther.monitorDetailId)
				&& this.pluginId.equals(castOther.pluginId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.monitorDetailId.hashCode();
		hash = hash * prime + this.pluginId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"monitorId",
				"monitorDetailId",
				"pluginId"
		};
		String[] values = {
				this.facilityId,
				this.monitorId,
				this.monitorDetailId,
				this.pluginId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}