/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.entity;

/**
 * Primary key for CollectorDeviceInfo.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class CollectorDeviceInfoPK extends java.lang.Object implements java.io.Serializable {
	private static final long serialVersionUID = -5980458622376116178L;

	public java.lang.String collectorId;
	public java.lang.String facilityId;
	public java.lang.String deviceName;

	public CollectorDeviceInfoPK() {
	}

	public CollectorDeviceInfoPK(java.lang.String collectorId, java.lang.String facilityId,
			java.lang.String deviceName) {
		this.collectorId = collectorId;
		this.facilityId = facilityId;
		this.deviceName = deviceName;
	}

	public java.lang.String getCollectorId() {
		return collectorId;
	}

	public java.lang.String getFacilityId() {
		return facilityId;
	}

	public java.lang.String getDeviceName() {
		return deviceName;
	}

	public void setCollectorId(java.lang.String collectorId) {
		this.collectorId = collectorId;
	}

	public void setFacilityId(java.lang.String facilityId) {
		this.facilityId = facilityId;
	}

	public void setDeviceName(java.lang.String deviceName) {
		this.deviceName = deviceName;
	}

	@Override
	public int hashCode() {
		int _hashCode = 0;
		if (this.collectorId != null)
			_hashCode += this.collectorId.hashCode();
		if (this.facilityId != null)
			_hashCode += this.facilityId.hashCode();
		if (this.deviceName != null)
			_hashCode += this.deviceName.hashCode();

		return _hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof com.clustercontrol.performance.monitor.entity.CollectorDeviceInfoPK))
			return false;

		com.clustercontrol.performance.monitor.entity.CollectorDeviceInfoPK pk = (com.clustercontrol.performance.monitor.entity.CollectorDeviceInfoPK) obj;
		boolean eq = true;

		if (this.collectorId != null) {
			eq = eq && this.collectorId.equals(pk.getCollectorId());
		} else // this.collectorId == null
		{
			eq = eq && (pk.getCollectorId() == null);
		}
		if (this.facilityId != null) {
			eq = eq && this.facilityId.equals(pk.getFacilityId());
		} else // this.facilityId == null
		{
			eq = eq && (pk.getFacilityId() == null);
		}
		if (this.deviceName != null) {
			eq = eq && this.deviceName.equals(pk.getDeviceName());
		} else // this.deviceName == null
		{
			eq = eq && (pk.getDeviceName() == null);
		}

		return eq;
	}

	/**
	 * @return String representation of this pk in the form of
	 *         [.field1.field2.field3].
	 */
	@Override
	public String toString() {
		StringBuffer toStringValue = new StringBuffer("[.");
		toStringValue.append(this.collectorId).append('.');
		toStringValue.append(this.facilityId).append('.');
		toStringValue.append(this.deviceName).append('.');
		toStringValue.append(']');
		return toStringValue.toString();
	}

}
