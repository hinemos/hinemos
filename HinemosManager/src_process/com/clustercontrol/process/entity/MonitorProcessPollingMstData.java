/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.process.entity;

/**
 * Data object for MonitorProcessPollingMst.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class MonitorProcessPollingMstData extends java.lang.Object implements java.io.Serializable {
	private static final long serialVersionUID = 1715583438928081477L;

	private java.lang.String collectMethod;
	private java.lang.String platformId;
	private java.lang.String subPlatformId;
	private java.lang.String variableId;
	private java.lang.String entryKey;
	private java.lang.String pollingTarget;

	/* begin value object */

	/* end value object */

	public MonitorProcessPollingMstData() {
	}

	public MonitorProcessPollingMstData(java.lang.String collectMethod, java.lang.String platformId,
			java.lang.String subPlatformId, java.lang.String variableId, java.lang.String entryKey,
			java.lang.String pollingTarget) {
		setCollectMethod(collectMethod);
		setPlatformId(platformId);
		setSubPlatformId(subPlatformId);
		setVariableId(variableId);
		setEntryKey(entryKey);
		setPollingTarget(pollingTarget);
	}

	public MonitorProcessPollingMstData(MonitorProcessPollingMstData otherData) {
		setCollectMethod(otherData.getCollectMethod());
		setPlatformId(otherData.getPlatformId());
		setSubPlatformId(otherData.getSubPlatformId());
		setVariableId(otherData.getVariableId());
		setEntryKey(otherData.getEntryKey());
		setPollingTarget(otherData.getPollingTarget());

	}

	public com.clustercontrol.process.entity.MonitorProcessPollingMstPK getPrimaryKey() {
		com.clustercontrol.process.entity.MonitorProcessPollingMstPK pk = new com.clustercontrol.process.entity.MonitorProcessPollingMstPK(
				this.getCollectMethod(), this.getPlatformId(), this.getSubPlatformId(), this.getVariableId());
		return pk;
	}

	public java.lang.String getCollectMethod() {
		return this.collectMethod;
	}

	public void setCollectMethod(java.lang.String collectMethod) {
		this.collectMethod = collectMethod;
	}

	public java.lang.String getPlatformId() {
		return this.platformId;
	}

	public void setPlatformId(java.lang.String platformId) {
		this.platformId = platformId;
	}

	public java.lang.String getSubPlatformId() {
		return this.subPlatformId;
	}

	public void setSubPlatformId(java.lang.String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	public java.lang.String getVariableId() {
		return this.variableId;
	}

	public void setVariableId(java.lang.String variableId) {
		this.variableId = variableId;
	}

	public java.lang.String getEntryKey() {
		return this.entryKey;
	}

	public void setEntryKey(java.lang.String entryKey) {
		this.entryKey = entryKey;
	}

	public java.lang.String getPollingTarget() {
		return this.pollingTarget;
	}

	public void setPollingTarget(java.lang.String pollingTarget) {
		this.pollingTarget = pollingTarget;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("collectMethod=" + getCollectMethod() + " " + "platformId=" + getPlatformId() + " "
				+ "subPlatformId=" + getSubPlatformId() + " " + "variableId=" + getVariableId() + " " + "entryKey="
				+ getEntryKey() + " " + "pollingTarget=" + getPollingTarget());
		str.append('}');

		return (str.toString());
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther instanceof MonitorProcessPollingMstData) {
			MonitorProcessPollingMstData lTest = (MonitorProcessPollingMstData) pOther;
			boolean lEquals = true;

			if (this.collectMethod == null) {
				lEquals = lEquals && (lTest.collectMethod == null);
			} else {
				lEquals = lEquals && this.collectMethod.equals(lTest.collectMethod);
			}
			if (this.platformId == null) {
				lEquals = lEquals && (lTest.platformId == null);
			} else {
				lEquals = lEquals && this.platformId.equals(lTest.platformId);
			}
			if (this.subPlatformId == null) {
				lEquals = lEquals && (lTest.subPlatformId == null);
			} else {
				lEquals = lEquals && this.subPlatformId.equals(lTest.subPlatformId);
			}
			if (this.variableId == null) {
				lEquals = lEquals && (lTest.variableId == null);
			} else {
				lEquals = lEquals && this.variableId.equals(lTest.variableId);
			}
			if (this.entryKey == null) {
				lEquals = lEquals && (lTest.entryKey == null);
			} else {
				lEquals = lEquals && this.entryKey.equals(lTest.entryKey);
			}
			if (this.pollingTarget == null) {
				lEquals = lEquals && (lTest.pollingTarget == null);
			} else {
				lEquals = lEquals && this.pollingTarget.equals(lTest.pollingTarget);
			}

			return lEquals;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + ((this.collectMethod != null) ? this.collectMethod.hashCode() : 0);

		result = 37 * result + ((this.platformId != null) ? this.platformId.hashCode() : 0);

		result = 37 * result + ((this.subPlatformId != null) ? this.subPlatformId.hashCode() : 0);

		result = 37 * result + ((this.variableId != null) ? this.variableId.hashCode() : 0);

		result = 37 * result + ((this.entryKey != null) ? this.entryKey.hashCode() : 0);

		result = 37 * result + ((this.pollingTarget != null) ? this.pollingTarget.hashCode() : 0);

		return result;
	}

}
