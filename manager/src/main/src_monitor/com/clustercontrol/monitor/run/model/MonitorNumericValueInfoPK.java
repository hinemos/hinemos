/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_monitor_numeric_value_info database table.
 * 
 */
@Embeddable
public class MonitorNumericValueInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private String monitorNumericType;
	private Integer priority;

	public MonitorNumericValueInfoPK() {
	}

	public MonitorNumericValueInfoPK(String monitorId, String monitorNumericType, Integer priority) {
		this.setMonitorId(monitorId);
		this.setMonitorNumericType(monitorNumericType);
		this.setPriority(priority);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="monitor_numeric_type")
	public String getMonitorNumericType() {
		return this.monitorNumericType;
	}
	public void setMonitorNumericType(String monitorNumericType) {
		this.monitorNumericType = monitorNumericType;
	}

	@Column(name="priority")
	public Integer getPriority() {
		return this.priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorNumericValueInfoPK)) {
			return false;
		}
		MonitorNumericValueInfoPK castOther = (MonitorNumericValueInfoPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.monitorNumericType.equals(castOther.monitorNumericType)
				&& this.priority.equals(castOther.priority);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.monitorNumericType.hashCode();
		hash = hash * prime + this.priority.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"monitorNumericType",
				"priority"
		};
		String[] values = {
				this.monitorId,
				this.monitorNumericType,
				this.priority.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}