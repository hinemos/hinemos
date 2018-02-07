/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.util.CollectionComparator;


public class StorageBackupEntry extends Element implements IStorageBackupEntry {
	private String Id;
	private String name;
	private String status;
	private String statusAsPlatform;
	private String description;
	private Long createTime;
	
	private List<BackupedDataEntry> backupedDataEntries = new ArrayList<>();

	@Override
	public String getId() {
		return Id;
	}
	public void setId(String Id) {
		this.Id = Id;
	}

	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) { internalSetProperty(IStorageBackupEntry.p.name, name, ()->this.name, (s)->this.name=s);}

	@Override
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {internalSetProperty(IStorageBackupEntry.p.status, status, ()->this.status, (s)->this.status=s);}

	@Override
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {internalSetProperty(IStorageBackupEntry.p.createTime, createTime, ()->this.createTime, (s)->this.createTime=s); }

	@Override
	public String getStatusAsPlatform() {
		return statusAsPlatform;
	}
	public void setStatusAsPlatform(String statusAsPlatform) {internalSetProperty(IStorageBackupEntry.p.statusAsPlatform, statusAsPlatform, ()->this.statusAsPlatform, (s)->this.statusAsPlatform=s);}

	@Override
	public StorageBackup getBackup() {
		return (StorageBackup)getOwner();
	}

	@Override
	public String getDescription() {
		return description;
	}
	public void setDescription(String detail) {internalSetProperty(IStorageBackupEntry.p.description, detail, ()->this.description, (s)->this.description=s);}
	
	@Override
	public BackupedDataEntry[] getBackupedDataEntries() {
		return backupedDataEntries.toArray(new BackupedDataEntry[backupedDataEntries.size()]);
	}
	@Override
	public String getBackupedDataEntryValue(String name) {
		for (BackupedDataEntry property: backupedDataEntries) {
			if (property.getName().equals(name))
				return property.getValue();
		}
		return null;
	}
	
	public static StorageBackupEntry convert(com.clustercontrol.ws.xcloud.StorageBackupEntry source) {
		StorageBackupEntry entry = new StorageBackupEntry();
		entry.update(source);
		return entry;
	}
	
	public void update(com.clustercontrol.ws.xcloud.StorageBackupEntry source) {
		setId(source.getId());
		setName(source.getName());
		setStatus(source.getStatus().value());
		setStatusAsPlatform(source.getStatusAsPlatform());
		setDescription(source.getDescription());
		setCreateTime(source.getCreateTime());
		updateBackupedDataEntries(source.getBackupedData().getEntries());
	}
	
	protected void updateBackupedDataEntries(List<com.clustercontrol.ws.xcloud.BackupedDataEntry> extendedProperties) {
		CollectionComparator.compareCollection(this.backupedDataEntries, extendedProperties, new CollectionComparator.Comparator<BackupedDataEntry, com.clustercontrol.ws.xcloud.BackupedDataEntry>(){
			@Override
			public boolean match(BackupedDataEntry o1, com.clustercontrol.ws.xcloud.BackupedDataEntry o2) {
				return o1.getName().equals(o2.getName());
			}
			@Override
			public void matched(BackupedDataEntry o1, com.clustercontrol.ws.xcloud.BackupedDataEntry o2) {
				o1.setValue(o2.getValue());
			}
			@Override
			public void afterO1(BackupedDataEntry o1) {
				internalRemoveProperty(p.backupedDataEntries, o1, backupedDataEntries);
			}
			@Override
			public void afterO2(com.clustercontrol.ws.xcloud.BackupedDataEntry o2) {
				internalAddProperty(p.backupedDataEntries, BackupedDataEntry.convert(o2), backupedDataEntries);
			}
		});
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return getBackup().getAdapter(adapter);
	}
}
