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

public class StorageBackupInfoResponse {
	private String cloudScopeId;
	private String locationId;
	private String storageId;
	private List<StorageBackupEntryResponse> entries = new ArrayList<>();

	public StorageBackupInfoResponse() {
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

	public String getStorageId() {
		return storageId;
	}

	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}

	public List<StorageBackupEntryResponse> getEntries() {
		return entries;
	}

	public void setEntries(List<StorageBackupEntryResponse> entries) {
		this.entries = entries;
	}
}
