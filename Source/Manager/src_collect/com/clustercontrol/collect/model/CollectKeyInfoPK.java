/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.model;

import java.io.Serializable;
import java.util.Arrays;

import jakarta.persistence.*;

@Embeddable
public class CollectKeyInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String itemName;
	private String displayName;
	private String monitorId;
	private String facilityId;

	public CollectKeyInfoPK() {
	}

	public CollectKeyInfoPK(String item_name, String display_name, String monitor_id, String facilityid) {
		this.setItemName(item_name);
		this.setDisplayName(display_name);
		this.setMonitorId(monitor_id);
		this.setFacilityid(facilityid);
	}
	
	@Column(name="item_name")
	public String getItemName() {
		return this.itemName;
	}
	public void setItemName(String item_name) {
		this.itemName = item_name;
	}
	
	@Column(name="display_name")
	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String display_name) {
		this.displayName = display_name;
	}
	
	@Column(name="monitor_id")
	public String getMonitorId() {
		return this.monitorId;
	}
	public void setMonitorId(String monitor_id) {
		this.monitorId = monitor_id;
	}

	@Column(name="facility_id")
	public String getFacilityid() {
		return this.facilityId;
	}

	public void setFacilityid(String facilityid) {
		this.facilityId = facilityid;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof CollectKeyInfoPK)) {
			return false;
		}
		CollectKeyInfoPK castOther = (CollectKeyInfoPK)other;
		return
				this.itemName.equals(castOther.itemName)
				&& this.displayName.equals(castOther.displayName)
				&& this.monitorId.equals(castOther.monitorId)
				&& this.facilityId.equals(castOther.facilityId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.itemName.hashCode();
		hash = hash * prime + this.displayName.hashCode();
		hash = hash * prime + this.monitorId.hashCode();
		hash = hash * prime + this.facilityId.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"item_name",
				"display_name",
				"monitor_id",
				"facilityid"
		};
		String[] values = {
				this.itemName,
				this.displayName,
				this.monitorId,
				this.facilityId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}