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

public class HRepositoryResponse {
	private List<CloudPlatformInfoResponse> platforms = new ArrayList<>();
	private List<CloudScopeInfoResponse> cloudScopes = new ArrayList<>();
	private List<CloudLoginUserInfoResponse> loginUsers = new ArrayList<>();
	private List<HFacilityResponse> facilities = new ArrayList<>();
	private List<InstanceInfoResponse> instances = new ArrayList<>();
	private List<InstanceBackupResponse> instanceBackups = new ArrayList<>();
	private List<StorageInfoResponse> storages = new ArrayList<>();
	private List<StorageBackupInfoResponse> storageBackups = new ArrayList<>();

	public HRepositoryResponse() {
	}

	public List<CloudPlatformInfoResponse> getPlatforms() {
		return platforms;
	}

	public void setPlatforms(List<CloudPlatformInfoResponse> platforms) {
		this.platforms = platforms;
	}

	public List<CloudScopeInfoResponse> getCloudScopes() {
		return cloudScopes;
	}

	public void setCloudScopes(List<CloudScopeInfoResponse> cloudScopes) {
		this.cloudScopes = cloudScopes;
	}

	public List<CloudLoginUserInfoResponse> getLoginUsers() {
		return loginUsers;
	}

	public void setLoginUsers(List<CloudLoginUserInfoResponse> loginUsers) {
		this.loginUsers = loginUsers;
	}

	public List<HFacilityResponse> getFacilities() {
		return facilities;
	}

	public void setFacilities(List<HFacilityResponse> facilities) {
		this.facilities = facilities;
	}

	public List<InstanceInfoResponse> getInstances() {
		return instances;
	}

	public void setInstances(List<InstanceInfoResponse> instances) {
		this.instances = instances;
	}

	public List<InstanceBackupResponse> getInstanceBackups() {
		return instanceBackups;
	}

	public void setInstanceBackups(List<InstanceBackupResponse> instanceBackups) {
		this.instanceBackups = instanceBackups;
	}

	public List<StorageInfoResponse> getStorages() {
		return storages;
	}

	public void setStorages(List<StorageInfoResponse> storages) {
		this.storages = storages;
	}

	public List<StorageBackupInfoResponse> getStorageBackups() {
		return storageBackups;
	}

	public void setStorageBackups(List<StorageBackupInfoResponse> storageBackups) {
		this.storageBackups = storageBackups;
	}

}
