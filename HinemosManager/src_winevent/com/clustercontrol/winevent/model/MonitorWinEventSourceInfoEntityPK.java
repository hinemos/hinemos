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
 * The primary key class for the cc_monitor_winevent_source_info database table.
 * 
 */
@Embeddable
public class MonitorWinEventSourceInfoEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private String source;

	public MonitorWinEventSourceInfoEntityPK() {
	}

	public MonitorWinEventSourceInfoEntityPK(String monitorId, String source) {
		this.setMonitorId(monitorId);
		this.setSource(source);
	}

	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Column(name="source")
	public String getSource(){
		return this.source;
	}
	public void setSource(String source){
		this.source = source;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MonitorWinEventSourceInfoEntityPK)) {
			return false;
		}
		MonitorWinEventSourceInfoEntityPK castOther = (MonitorWinEventSourceInfoEntityPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.source.equals(castOther.source);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.source.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"monitorId",
				"source"
		};
		String[] values = {
				this.monitorId,
				this.source.toString()
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}