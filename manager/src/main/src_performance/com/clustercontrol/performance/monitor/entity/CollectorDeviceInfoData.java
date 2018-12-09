/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.performance.monitor.entity;

/**
 * Data object for CollectorDeviceInfo.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class CollectorDeviceInfoData extends java.lang.Object implements java.io.Serializable {
	private static final long serialVersionUID = -8893077544563183521L;

	private java.lang.String collectorId;
	private java.lang.String facilityId;
	private java.lang.String deviceName;
	private java.lang.String displayName;
	private java.lang.Long index;
	private java.lang.String deviceId;
	private java.lang.String deviceType;

	/* begin value object */

	/* end value object */

	public CollectorDeviceInfoData() {
	}

	public CollectorDeviceInfoData(java.lang.String collectorId, java.lang.String facilityId,
			java.lang.String deviceName, java.lang.String displayName, java.lang.Long index, java.lang.String deviceId,
			java.lang.String deviceType) {
		setCollectorId(collectorId);
		setFacilityId(facilityId);
		setDeviceName(deviceName);
		setDisplayName(displayName);
		setIndex(index);
		setDeviceId(deviceId);
		setDeviceType(deviceType);
	}

	public CollectorDeviceInfoData(CollectorDeviceInfoData otherData) {
		setCollectorId(otherData.getCollectorId());
		setFacilityId(otherData.getFacilityId());
		setDeviceName(otherData.getDeviceName());
		setDisplayName(otherData.getDisplayName());
		setIndex(otherData.getIndex());
		setDeviceId(otherData.getDeviceId());
		setDeviceType(otherData.getDeviceType());

	}

	public com.clustercontrol.performance.monitor.entity.CollectorDeviceInfoPK getPrimaryKey() {
		com.clustercontrol.performance.monitor.entity.CollectorDeviceInfoPK pk = new com.clustercontrol.performance.monitor.entity.CollectorDeviceInfoPK(
				this.getCollectorId(), this.getFacilityId(), this.getDeviceName());
		return pk;
	}

	public java.lang.String getCollectorId() {
		return this.collectorId;
	}

	public void setCollectorId(java.lang.String collectorId) {
		this.collectorId = collectorId;
	}

	public java.lang.String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(java.lang.String facilityId) {
		this.facilityId = facilityId;
	}

	public java.lang.String getDeviceName() {
		return this.deviceName;
	}

	public void setDeviceName(java.lang.String deviceName) {
		this.deviceName = deviceName;
	}

	public java.lang.String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(java.lang.String displayName) {
		this.displayName = displayName;
	}

	public java.lang.Long getIndex() {
		return this.index;
	}

	public void setIndex(java.lang.Long index) {
		this.index = index;
	}

	public java.lang.String getDeviceId() {
		return this.deviceId;
	}

	public void setDeviceId(java.lang.String deviceId) {
		this.deviceId = deviceId;
	}

	public java.lang.String getDeviceType() {
		return this.deviceType;
	}

	public void setDeviceType(java.lang.String deviceType) {
		this.deviceType = deviceType;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("collectorId=" + getCollectorId() + " " + "facilityId=" + getFacilityId() + " " + "deviceName="
				+ getDeviceName() + " " + "displayName=" + getDisplayName() + " " + "index=" + getIndex() + " "
				+ "deviceId=" + getDeviceId() + " " + "deviceType=" + getDeviceType());
		str.append('}');

		return (str.toString());
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther instanceof CollectorDeviceInfoData) {
			CollectorDeviceInfoData lTest = (CollectorDeviceInfoData) pOther;
			boolean lEquals = true;

			if (this.collectorId == null) {
				lEquals = lEquals && (lTest.collectorId == null);
			} else {
				lEquals = lEquals && this.collectorId.equals(lTest.collectorId);
			}
			if (this.facilityId == null) {
				lEquals = lEquals && (lTest.facilityId == null);
			} else {
				lEquals = lEquals && this.facilityId.equals(lTest.facilityId);
			}
			if (this.deviceName == null) {
				lEquals = lEquals && (lTest.deviceName == null);
			} else {
				lEquals = lEquals && this.deviceName.equals(lTest.deviceName);
			}
			if (this.displayName == null) {
				lEquals = lEquals && (lTest.displayName == null);
			} else {
				lEquals = lEquals && this.displayName.equals(lTest.displayName);
			}
			if (this.index == null) {
				lEquals = lEquals && (lTest.index == null);
			} else {
				lEquals = lEquals && this.index.equals(lTest.index);
			}
			if (this.deviceId == null) {
				lEquals = lEquals && (lTest.deviceId == null);
			} else {
				lEquals = lEquals && this.deviceId.equals(lTest.deviceId);
			}
			if (this.deviceType == null) {
				lEquals = lEquals && (lTest.deviceType == null);
			} else {
				lEquals = lEquals && this.deviceType.equals(lTest.deviceType);
			}

			return lEquals;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + ((this.collectorId != null) ? this.collectorId.hashCode() : 0);

		result = 37 * result + ((this.facilityId != null) ? this.facilityId.hashCode() : 0);

		result = 37 * result + ((this.deviceName != null) ? this.deviceName.hashCode() : 0);

		result = 37 * result + ((this.displayName != null) ? this.displayName.hashCode() : 0);

		result = 37 * result + ((this.index != null) ? this.index.hashCode() : 0);

		result = 37 * result + ((this.deviceId != null) ? this.deviceId.hashCode() : 0);

		result = 37 * result + ((this.deviceType != null) ? this.deviceType.hashCode() : 0);

		return result;
	}

}
