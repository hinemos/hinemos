/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * The primary key class for the cc_monitor_winevent_log_info database table.
 * 
 */
@Embeddable
public class MonitorWinEventLogInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private String logName;

	public MonitorWinEventLogInfoEntityPK() {
	}

	public MonitorWinEventLogInfoEntityPK(String monitorId, String logName) {
		this.setMonitorId(monitorId);
		this.setLogName(logName);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="log_name")
	public String getLogName(){
		return this.logName;
	}
	public void setLogName(String logName){
		this.logName = logName;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorWinEventLogInfoEntityPK)) {
			return false;
		}
		MonitorWinEventLogInfoEntityPK castOther = (MonitorWinEventLogInfoEntityPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.logName.equals(castOther.logName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.logName.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"logName"
		};
		String[] values = {
				this.monitorId,
				this.logName.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}