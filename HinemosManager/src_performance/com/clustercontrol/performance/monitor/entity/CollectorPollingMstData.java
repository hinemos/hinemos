/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.entity;

/**
 * Data object for CollectorPollingMst.
 * 
 * @version 6.1.0
 * @since 4.0.1
 */
public class CollectorPollingMstData extends java.lang.Object implements java.io.Serializable {
	private static final long serialVersionUID = 265764249848052818L;

	private java.lang.String collectMethod;
	private java.lang.String platformId;
	private java.lang.String subPlatformId;
	private java.lang.String itemCode;
	private java.lang.String variableId;
	private java.lang.String entryKey;
	private java.lang.String valueType;
	private java.lang.String pollingTarget;
	private java.lang.String failureValue;

	/* begin value object */

	/* end value object */

	public CollectorPollingMstData() {
	}

	public CollectorPollingMstData(java.lang.String collectMethod, java.lang.String platformId,
			java.lang.String subPlatformId, java.lang.String itemCode, java.lang.String variableId,
			java.lang.String entryKey, java.lang.String valueType, java.lang.String pollingTarget,
			java.lang.String failureValue) {
		setCollectMethod(collectMethod);
		setPlatformId(platformId);
		setSubPlatformId(subPlatformId);
		setItemCode(itemCode);
		setVariableId(variableId);
		setEntryKey(entryKey);
		setValueType(valueType);
		setPollingTarget(pollingTarget);
		setFailureValue(failureValue);
	}

	public CollectorPollingMstData(CollectorPollingMstData otherData) {
		setCollectMethod(otherData.getCollectMethod());
		setPlatformId(otherData.getPlatformId());
		setSubPlatformId(otherData.getSubPlatformId());
		setItemCode(otherData.getItemCode());
		setVariableId(otherData.getVariableId());
		setEntryKey(otherData.getEntryKey());
		setValueType(otherData.getValueType());
		setPollingTarget(otherData.getPollingTarget());
		setFailureValue(otherData.getFailureValue());

	}

	public com.clustercontrol.performance.monitor.entity.CollectorPollingMstPK getPrimaryKey() {
		com.clustercontrol.performance.monitor.entity.CollectorPollingMstPK pk = new com.clustercontrol.performance.monitor.entity.CollectorPollingMstPK(
				this.getCollectMethod(), this.getPlatformId(), this.getSubPlatformId(), this.getItemCode(),
				this.getVariableId());
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

	public java.lang.String getItemCode() {
		return this.itemCode;
	}

	public void setItemCode(java.lang.String itemCode) {
		this.itemCode = itemCode;
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

	public java.lang.String getValueType() {
		return this.valueType;
	}

	public void setValueType(java.lang.String valueType) {
		this.valueType = valueType;
	}

	public java.lang.String getPollingTarget() {
		return this.pollingTarget;
	}

	public void setPollingTarget(java.lang.String pollingTarget) {
		this.pollingTarget = pollingTarget;
	}

	public java.lang.String getFailureValue() {
		return this.failureValue;
	}

	public void setFailureValue(java.lang.String failureValue) {
		this.failureValue = failureValue;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer("{");

		str.append("collectMethod=" + getCollectMethod() + " " + "platformId=" + getPlatformId() + " "
				+ "subPlatformId=" + getSubPlatformId() + " " + "itemCode=" + getItemCode() + " " + "variableId="
				+ getVariableId() + " " + "entryKey=" + getEntryKey() + " " + "valueType=" + getValueType() + " "
				+ "pollingTarget=" + getPollingTarget() + " " + "failureValue=" + getFailureValue());
		str.append('}');

		return (str.toString());
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther instanceof CollectorPollingMstData) {
			CollectorPollingMstData lTest = (CollectorPollingMstData) pOther;
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
			if (this.itemCode == null) {
				lEquals = lEquals && (lTest.itemCode == null);
			} else {
				lEquals = lEquals && this.itemCode.equals(lTest.itemCode);
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
			if (this.valueType == null) {
				lEquals = lEquals && (lTest.valueType == null);
			} else {
				lEquals = lEquals && this.valueType.equals(lTest.valueType);
			}
			if (this.pollingTarget == null) {
				lEquals = lEquals && (lTest.pollingTarget == null);
			} else {
				lEquals = lEquals && this.pollingTarget.equals(lTest.pollingTarget);
			}
			if (this.failureValue == null) {
				lEquals = lEquals && (lTest.failureValue == null);
			} else {
				lEquals = lEquals && this.failureValue.equals(lTest.failureValue);
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

		result = 37 * result + ((this.itemCode != null) ? this.itemCode.hashCode() : 0);

		result = 37 * result + ((this.variableId != null) ? this.variableId.hashCode() : 0);

		result = 37 * result + ((this.entryKey != null) ? this.entryKey.hashCode() : 0);

		result = 37 * result + ((this.valueType != null) ? this.valueType.hashCode() : 0);

		result = 37 * result + ((this.pollingTarget != null) ? this.pollingTarget.hashCode() : 0);

		result = 37 * result + ((this.failureValue != null) ? this.failureValue.hashCode() : 0);

		return result;
	}

}
