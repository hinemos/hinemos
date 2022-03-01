/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

public class CollectorPollingMstDataResponse {
	private String collectMethod;
	private String platformId;
	private String subPlatformId;
	private String itemCode;
	private String variableId;
	private String entryKey;
	private String valueType;
	private String pollingTarget;
	private String failureValue;

	public CollectorPollingMstDataResponse() {
	}

	public String getCollectMethod() {
		return this.collectMethod;
	}

	public void setCollectMethod(String collectMethod) {
		this.collectMethod = collectMethod;
	}

	public String getPlatformId() {
		return this.platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getSubPlatformId() {
		return this.subPlatformId;
	}

	public void setSubPlatformId(String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	public String getItemCode() {
		return this.itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getVariableId() {
		return this.variableId;
	}

	public void setVariableId(String variableId) {
		this.variableId = variableId;
	}

	public String getEntryKey() {
		return this.entryKey;
	}

	public void setEntryKey(String entryKey) {
		this.entryKey = entryKey;
	}

	public String getValueType() {
		return this.valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public String getPollingTarget() {
		return this.pollingTarget;
	}

	public void setPollingTarget(String pollingTarget) {
		this.pollingTarget = pollingTarget;
	}

	public String getFailureValue() {
		return this.failureValue;
	}

	public void setFailureValue(String failureValue) {
		this.failureValue = failureValue;
	}
}
