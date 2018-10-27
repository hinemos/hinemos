/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CollectStringKeyInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String monitorId;
	private String facilityId;
	private String targetName;

	public CollectStringKeyInfoPK() {
	}

	public CollectStringKeyInfoPK(String monitor_id, String facilityid, String targetName) {
		this.setMonitorId(monitor_id);
		this.setFacilityId(facilityid);
		this.setTargetName(targetName);
	}
	
	@Column(name="monitor_id")
	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitor_id) {
		this.monitorId = monitor_id;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="target_name")
	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CollectStringKeyInfoPK)) {
			return false;
		}
		CollectStringKeyInfoPK castOther = (CollectStringKeyInfoPK)other;
		return
				this.monitorId.equals(castOther.monitorId)
				&& this.facilityId.equals(castOther.facilityId)
				&& this.targetName.equals(castOther.targetName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.targetName.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return "CollectStringKeyInfoPK [monitorId=" + monitorId + ", facilityId=" + facilityId + ", targetName="
				+ targetName + "]";
	}
}