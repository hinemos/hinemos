/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.ArrayList;
import java.util.List;

public class InstanceBackupResponse {
	private String cloudScopeId;
	private String locationId;
	private String instanceId;
	private List<InstanceBackupEntryResponse> entries = new ArrayList<>();

	public InstanceBackupResponse() {
	}

	public String getCloudScopeId() {
		return cloudScopeId;
	}

	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public List<InstanceBackupEntryResponse> getEntries() {
		return entries;
	}

	public void setEntries(List<InstanceBackupEntryResponse> entries) {
		this.entries = entries;
	}

}
