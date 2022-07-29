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

import com.clustercontrol.xcloud.model.InstanceBackupEntity;
import com.clustercontrol.xcloud.model.InstanceBackupEntryEntity;

public class InstanceBackup {
	private String cloudScopeId;
	private String locationId;
	private String instanceId;
	private List<InstanceBackupEntry> entries = new ArrayList<>();
	
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

	public List<InstanceBackupEntry> getEntries() {
		return entries;
	}
	public void setEntries(List<InstanceBackupEntry> entries) {
		this.entries = entries;
	}	
	
	public static InstanceBackup convertWebEntity(InstanceBackupEntity entity) {
		InstanceBackup backup = new InstanceBackup();
		backup.setCloudScopeId(entity.getCloudScopeId());
		backup.setLocationId(entity.getLocationId());
		backup.setInstanceId(entity.getInstanceId());
		for (InstanceBackupEntryEntity entryEntity: entity.getEntries()) {
			backup.getEntries().add(InstanceBackupEntry.convertWebEntity(entryEntity));
		}
		return backup;
	}
	
	public static List<InstanceBackup> convertWebEntities(List<InstanceBackupEntity> entities) {
		List<InstanceBackup> webEntries = new ArrayList<>();
		for (InstanceBackupEntity entity: entities) {
			webEntries.add(convertWebEntity(entity));
		}
		return webEntries;
	}
}
