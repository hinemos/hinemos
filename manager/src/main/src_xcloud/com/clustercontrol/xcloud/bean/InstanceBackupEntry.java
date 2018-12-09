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
import com.clustercontrol.xcloud.factory.IResourceManagement.InstanceSnapshot.InstanceSnapshotStatusType;
import com.clustercontrol.xcloud.model.InstanceBackupEntryEntity;

public class InstanceBackupEntry {
	private String id;
	private String name;
	private String description;
	
	private InstanceSnapshotStatusType status;
	private String statudAsPlatform;
	private Long createTime;
	
	private BackupedData backupedData;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public InstanceSnapshotStatusType getStatus() {
		return status;
	}
	public void setStatus(InstanceSnapshotStatusType status) {
		this.status = status;
	}
	public String getStatusAsPlatform() {
		return statudAsPlatform;
	}
	public void setStatusAsPlatform(String statudAsPlatform) {
		this.statudAsPlatform = statudAsPlatform;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public BackupedData getBackupedData() {
		return backupedData;
	}
	public void setBackupedData(BackupedData backupedData) {
		this.backupedData = backupedData;
	}
	
	public static InstanceBackupEntry convertWebEntity(InstanceBackupEntryEntity entity) {
		InstanceBackupEntry newEntry = new InstanceBackupEntry();
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
	
	public static List<InstanceBackupEntry> convertWebEntities(List<InstanceBackupEntryEntity> entities) {
		List<InstanceBackupEntry> webEntries = new ArrayList<>();
		for (InstanceBackupEntryEntity entity: entities) {
			webEntries.add(convertWebEntity(entity));
		}
		return webEntries;
	}
}
