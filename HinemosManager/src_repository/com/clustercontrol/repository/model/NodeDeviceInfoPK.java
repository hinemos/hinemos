/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.Arrays;

import jakarta.persistence.*;

/**
 * The primary key class for the cc_cfg_node_disk database table.
 * 
 */
@Embeddable
public class NodeDeviceInfoPK implements Serializable, Cloneable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId = "";
	private Integer deviceIndex = -1;
	private String deviceType = "";
	private String deviceName = "";

	public NodeDeviceInfoPK() {
	}

	public NodeDeviceInfoPK(String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName) {
		this.setFacilityId(facilityId);
		this.setDeviceIndex(deviceIndex);
		this.setDeviceType(deviceType);
		this.setDeviceName(deviceName);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="device_index")
	public Integer getDeviceIndex() {
		return this.deviceIndex;
	}
	public void setDeviceIndex(Integer deviceIndex) {
		this.deviceIndex = deviceIndex;
	}

	@Column(name="device_type")
	public String getDeviceType() {
		return this.deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	@Column(name="device_name")
	public String getDeviceName() {
		return this.deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((deviceIndex == null) ? 0 : deviceIndex.hashCode());
		result = prime * result
				+ ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result
				+ ((deviceType == null) ? 0 : deviceType.hashCode());
		result = prime * result
				+ ((facilityId == null) ? 0 : facilityId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeDeviceInfoPK other = (NodeDeviceInfoPK) obj;
		if (deviceIndex == null) {
			if (other.deviceIndex != null)
				return false;
		} else if (!deviceIndex.equals(other.deviceIndex))
			return false;
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		if (deviceType == null) {
			if (other.deviceType != null)
				return false;
		} else if (!deviceType.equals(other.deviceType))
			return false;
		if (facilityId == null) {
			if (other.facilityId != null)
				return false;
		} else if (!facilityId.equals(other.facilityId))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"deviceIndex",
				"deviceType",
				"deviceName"
		};
		String[] values = {
				this.facilityId,
				this.deviceIndex.toString(),
				this.deviceType,
				this.deviceName
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
	
	@Override
	public NodeDeviceInfoPK clone() {
		try {
			NodeDeviceInfoPK cloneInfo = (NodeDeviceInfoPK)super.clone();
			cloneInfo.facilityId = this.facilityId;
			cloneInfo.deviceIndex = this.deviceIndex;
			cloneInfo.deviceType = this.deviceType;
			cloneInfo.deviceName = this.deviceName;

			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}