/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.xcloud.model.StorageBackupEntity;
import com.clustercontrol.xcloud.model.StorageBackupEntryEntity;

public class StorageBackup {
	private String cloudScopeId;
	private String locationId;
	private String storageId;
	
	private List<StorageBackupEntry> entries = new ArrayList<>();

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

	public List<StorageBackupEntry> getEntries() {
		return entries;
	}
	public void setEntries(List<StorageBackupEntry> entries) {
		this.entries = entries;
	}

	public static StorageBackup convertWebEntity(StorageBackupEntity entity) {
		StorageBackup backup = new StorageBackup();
		backup.setCloudScopeId(entity.getCloudScopeId());
		backup.setLocationId(entity.getLocationId());
		backup.setStorageId(entity.getStorageId());
		for (StorageBackupEntryEntity entryEntity: entity.getEntries()) {
			backup.getEntries().add(StorageBackupEntry.convertWebEntity(entryEntity));
		}
		return backup;
	}
	
	public static List<StorageBackup> convertWebEntities(List<StorageBackupEntity> entities) {
		List<StorageBackup> webEntries = new ArrayList<>();
		for (StorageBackupEntity entity: entities) {
			webEntries.add(convertWebEntity(entity));
		}
		return webEntries;
	}
}
