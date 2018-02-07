/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.process.entity;

/**
 * Primary key for MonitorProcessPollingMst.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class MonitorProcessPollingMstPK extends java.lang.Object implements java.io.Serializable {

	private static final long serialVersionUID = -304852000962184459L;

	public java.lang.String collectMethod;
	public java.lang.String platformId;
	public java.lang.String subPlatformId;
	public java.lang.String variableId;

	public MonitorProcessPollingMstPK() {
	}

	public MonitorProcessPollingMstPK(java.lang.String collectMethod, java.lang.String platformId,
			java.lang.String subPlatformId, java.lang.String variableId) {
		this.collectMethod = collectMethod;
		this.platformId = platformId;
		this.subPlatformId = subPlatformId;
		this.variableId = variableId;
	}

	public java.lang.String getCollectMethod() {
		return collectMethod;
	}

	public java.lang.String getPlatformId() {
		return platformId;
	}

	public java.lang.String getSubPlatformId() {
		return subPlatformId;
	}

	public java.lang.String getVariableId() {
		return variableId;
	}

	public void setCollectMethod(java.lang.String collectMethod) {
		this.collectMethod = collectMethod;
	}

	public void setPlatformId(java.lang.String platformId) {
		this.platformId = platformId;
	}

	public void setSubPlatformId(java.lang.String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	public void setVariableId(java.lang.String variableId) {
		this.variableId = variableId;
	}

	@Override
	public int hashCode() {
		int _hashCode = 0;
		if (this.collectMethod != null)
			_hashCode += this.collectMethod.hashCode();
		if (this.platformId != null)
			_hashCode += this.platformId.hashCode();
		if (this.subPlatformId != null)
			_hashCode += this.subPlatformId.hashCode();
		if (this.variableId != null)
			_hashCode += this.variableId.hashCode();

		return _hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof com.clustercontrol.process.entity.MonitorProcessPollingMstPK))
			return false;

		com.clustercontrol.process.entity.MonitorProcessPollingMstPK pk = (com.clustercontrol.process.entity.MonitorProcessPollingMstPK) obj;
		boolean eq = true;

		if (this.collectMethod != null) {
			eq = eq && this.collectMethod.equals(pk.getCollectMethod());
		} else // this.collectMethod == null
		{
			eq = eq && (pk.getCollectMethod() == null);
		}
		if (this.platformId != null) {
			eq = eq && this.platformId.equals(pk.getPlatformId());
		} else // this.platformId == null
		{
			eq = eq && (pk.getPlatformId() == null);
		}
		if (this.subPlatformId != null) {
			eq = eq && this.subPlatformId.equals(pk.getSubPlatformId());
		} else // this.subPlatformId == null
		{
			eq = eq && (pk.getSubPlatformId() == null);
		}
		if (this.variableId != null) {
			eq = eq && this.variableId.equals(pk.getVariableId());
		} else // this.variableId == null
		{
			eq = eq && (pk.getVariableId() == null);
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
		toStringValue.append(this.collectMethod).append('.');
		toStringValue.append(this.platformId).append('.');
		toStringValue.append(this.subPlatformId).append('.');
		toStringValue.append(this.variableId).append('.');
		toStringValue.append(']');
		return toStringValue.toString();
	}

}
