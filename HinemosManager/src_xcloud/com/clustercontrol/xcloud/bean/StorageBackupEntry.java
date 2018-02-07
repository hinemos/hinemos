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

import com.clustercontrol.xcloud.bean.BackupedData.BackupedDataEntry;
import com.clustercontrol.xcloud.factory.IResourceManagement.StorageSnapshot.StorageSnapshotStatusType;
import com.clustercontrol.xcloud.model.StorageBackupEntryEntity;

public class StorageBackupEntry {
	private StorageBackup storageBackup;
	
	private String snapshotId;
	private String name;
	private StorageSnapshotStatusType status;
	private String statusAsPlatform;
	private Long createTime;
	private String desription;
	
	private BackupedData backupedData;
	
	public StorageBackup getStorageBackup() {
		return storageBackup;
	}
	public void setStorageBackup(StorageBackup storageBackup) {
		this.storageBackup = storageBackup;
	}
	public String getId() {
		return snapshotId;
	}
	public void setId(String snapshotId) {
		this.snapshotId = snapshotId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public StorageSnapshotStatusType getStatus() {
		return status;
	}
	public void setStatus(StorageSnapshotStatusType statsu) {
		this.status = statsu;
	}
	public String getStatusAsPlatform() {
		return statusAsPlatform;
	}
	public void setStatusAsPlatform(String statusDetail) {
		this.statusAsPlatform = statusDetail;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	public String getDescription() {
		return desription;
	}
	public void setDescription(String desription) {
		this.desription = desription;
	}
	
	public BackupedData getBackupedData() {
		return backupedData;
	}
	public void setBackupedData(BackupedData backupedData) {
		this.backupedData = backupedData;
	}
	
	public static StorageBackupEntry convertWebEntity(StorageBackupEntryEntity entity) {
		StorageBackupEntry newEntry = new StorageBackupEntry();
		newEntry.setId(entity.getEntryId());
		newEntry.setName(entity.getName());
		newEntry.setDescription(entity.getDescription());
		newEntry.setStatus(entity.getStatus());
		newEntry.setStatusAsPlatform(entity.getStatusAsPlatform());
		newEntry.setCreateTime(entity.getCreateTime());
		
		BackupedData backupedData = new BackupedData();
		newEntry.setBackupedData(backupedData);
		for (com.clustercontrol.xcloud.model.BackupedData data: entity.getBackupedData().values()) {
			BackupedDataEntry newDataEntry = new BackupedDataEntry();
			newDataEntry.setName(data.getName());
			newDataEntry.setValue(data.getValue());
			backupedData.getEntries().add(newDataEntry);
		}
		return newEntry;
	}
	
	public static List<StorageBackupEntry> convertWebEntities(List<StorageBackupEntryEntity> entities) {
		List<StorageBackupEntry> webEntries = new ArrayList<>();
		for (StorageBackupEntryEntity entity: entities) {
			webEntries.add(convertWebEntity(entity));
		}
		return webEntries;
	}
}
