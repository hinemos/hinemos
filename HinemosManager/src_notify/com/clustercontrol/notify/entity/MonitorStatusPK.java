/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.entity;

/**
 * Primary key for MonitorStatus.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class MonitorStatusPK extends java.lang.Object implements java.io.Serializable {
	private static final long serialVersionUID = -4927532455274831803L;

	public java.lang.String facilityId;
	public java.lang.String pluginId;
	public java.lang.String monitorId;
	public java.lang.String subKey;

	public MonitorStatusPK() {
	}

	public MonitorStatusPK(java.lang.String facilityId, java.lang.String pluginId, java.lang.String monitorId,
			java.lang.String subKey) {
		this.facilityId = facilityId;
		this.pluginId = pluginId;
		this.monitorId = monitorId;
		this.subKey = subKey;
	}

	public java.lang.String getFacilityId() {
		return facilityId;
	}

	public java.lang.String getPluginId() {
		return pluginId;
	}

	public java.lang.String getMonitorId() {
		return monitorId;
	}

	public java.lang.String getSubKey() {
		return subKey;
	}

	public void setFacilityId(java.lang.String facilityId) {
		this.facilityId = facilityId;
	}

	public void setPluginId(java.lang.String pluginId) {
		this.pluginId = pluginId;
	}

	public void setMonitorId(java.lang.String monitorId) {
		this.monitorId = monitorId;
	}

	public void setSubKey(java.lang.String subKey) {
		this.subKey = subKey;
	}

	@Override
	public int hashCode() {
		int _hashCode = 0;
		if (this.facilityId != null)
			_hashCode += this.facilityId.hashCode();
		if (this.pluginId != null)
			_hashCode += this.pluginId.hashCode();
		if (this.monitorId != null)
			_hashCode += this.monitorId.hashCode();
		if (this.subKey != null)
			_hashCode += this.subKey.hashCode();

		return _hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof com.clustercontrol.notify.entity.MonitorStatusPK))
			return false;

		com.clustercontrol.notify.entity.MonitorStatusPK pk = (com.clustercontrol.notify.entity.MonitorStatusPK) obj;
		boolean eq = true;

		if (this.facilityId != null) {
			eq = eq && this.facilityId.equals(pk.getFacilityId());
		} else // this.facilityId == null
		{
			eq = eq && (pk.getFacilityId() == null);
		}
		if (this.pluginId != null) {
			eq = eq && this.pluginId.equals(pk.getPluginId());
		} else // this.pluginId == null
		{
			eq = eq && (pk.getPluginId() == null);
		}
		if (this.monitorId != null) {
			eq = eq && this.monitorId.equals(pk.getMonitorId());
		} else // this.monitorId == null
		{
			eq = eq && (pk.getMonitorId() == null);
		}
		if (this.subKey != null) {
			eq = eq && this.subKey.equals(pk.getSubKey());
		} else // this.subKey == null
		{
			eq = eq && (pk.getSubKey() == null);
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
		toStringValue.append(this.facilityId).append('.');
		toStringValue.append(this.pluginId).append('.');
		toStringValue.append(this.monitorId).append('.');
		toStringValue.append(this.subKey).append('.');
		toStringValue.append(']');
		return toStringValue.toString();
	}

}
