/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.BackupedDataEntryResponse;
import org.openapitools.client.model.StorageBackupEntryResponse;

import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.util.CollectionComparator;


public class StorageBackupEntry extends Element implements IStorageBackupEntry {
	private static Log m_log = LogFactory.getLog(StorageBackupEntry.class);

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
	
	public static StorageBackupEntry convert(StorageBackupEntryResponse source) {
		StorageBackupEntry entry = new StorageBackupEntry();
		entry.update(source);
		return entry;
	}
	
	public void update(StorageBackupEntryResponse source) {
		setId(source.getSnapshotId());
		setName(source.getName());
		setStatus(source.getStatus().getValue());
		setStatusAsPlatform(source.getStatusAsPlatform());
		setDescription(source.getDesription());
		try {
			setCreateTime(TimezoneUtil.getSimpleDateFormat().parse(source.getCreateTime()).getTime());
		} catch (ParseException e) {
			// ここには入らない想定
			m_log.warn("invalid createTime.", e);
		}
		updateBackupedDataEntries(source.getBackupedData().getEntries());
	}
	
	protected void updateBackupedDataEntries(List<BackupedDataEntryResponse> extendedProperties) {
		CollectionComparator.compareCollection(this.backupedDataEntries, extendedProperties, new CollectionComparator.Comparator<BackupedDataEntry, BackupedDataEntryResponse>(){
			@Override
			public boolean match(BackupedDataEntry o1, BackupedDataEntryResponse o2) {
				return o1.getName().equals(o2.getName());
			}
			@Override
			public void matched(BackupedDataEntry o1, BackupedDataEntryResponse o2) {
				o1.setValue(o2.getValue());
			}
			@Override
			public void afterO1(BackupedDataEntry o1) {
				internalRemoveProperty(p.backupedDataEntries, o1, backupedDataEntries);
			}
			@Override
			public void afterO2(BackupedDataEntryResponse o2) {
				internalAddProperty(p.backupedDataEntries, BackupedDataEntry.convert(o2), backupedDataEntries);
			}
		});
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return getBackup().getAdapter(adapter);
	}
}
