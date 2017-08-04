package com.clustercontrol.collect.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

@Embeddable
public class CollectKeyInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String item_name;
	private String display_name;
	private String monitor_id;
	private String facility_id;

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
		return item_name;
	}
	public void setItemName(String item_name) {
		this.item_name = item_name;
	}
	
	@Column(name="display_name")
	public String getDisplayName() {
		return display_name;
	}

	public void setDisplayName(String display_name) {
		this.display_name = display_name;
	}
	
	@Column(name="monitor_id")
	public String getMonitorId() {
		return monitor_id;
	}
	public void setMonitorId(String monitor_id) {
		this.monitor_id = monitor_id;
	}

	@Column(name="facility_id")
	public String getFacilityid() {
		return facility_id;
	}

	public void setFacilityid(String facilityid) {
		this.facility_id = facilityid;
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
				this.item_name.equals(castOther.item_name)
				&& this.display_name.equals(castOther.display_name)
				&& this.monitor_id.equals(castOther.monitor_id)
				&& this.facility_id.equals(castOther.facility_id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.item_name.hashCode();
		hash = hash * prime + this.display_name.hashCode();
		hash = hash * prime + this.monitor_id.hashCode();
		hash = hash * prime + this.facility_id.hashCode();
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
				this.item_name,
				this.display_name,
				this.monitor_id,
				this.facility_id
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}