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
import org.openapitools.client.model.InstanceBackupEntryResponse;

import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.util.CollectionComparator;


public class InstanceBackupEntry extends Element implements IInstanceBackupEntry {
	private static Log m_log = LogFactory.getLog(InstanceBackupEntry.class);

	private String name;
	private String id;
	private String status;
	private String statusAsPlatform;
	private String description;
	private Long createTime;
	
	private List<BackupedDataEntry> backupedDataEntries = new ArrayList<>();
	
	@Override
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) { internalSetProperty(IInstanceBackupEntry.p.name, name, ()->this.name, (s)->this.name=s);}

	@Override
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {internalSetProperty(IInstanceBackupEntry.p.status, status, ()->this.status, (s)->this.status=s);}

	@Override
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {internalSetProperty(IInstanceBackupEntry.p.createTime, createTime,()->this.createTime,(s)->this.createTime=s); }

	@Override
	public String getStatusAsPlatform() {
		return statusAsPlatform;
	}
	public void setStatusAsPlatform(String statusAsPlatform) {internalSetProperty(IInstanceBackupEntry.p.statusAsPlatform, statusAsPlatform, ()->this.statusAsPlatform, (s)->this.statusAsPlatform=s);}

	@Override
	public InstanceBackup getBackup() {
		return (InstanceBackup)getOwner();
	}

	@Override
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {internalSetProperty(IInstanceBackupEntry.p.description, description, ()->this.description, (s)->this.description=s);}
	
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
	
	public static InstanceBackupEntry convert(InstanceBackupEntryResponse source) {
		InstanceBackupEntry entry = new InstanceBackupEntry();
		entry.update(source);
		return entry;
	}
	
	public void update(InstanceBackupEntryResponse source) {
		setId(source.getId());
		setName(source.getName());
		setStatus(source.getStatus().getValue());
		setStatusAsPlatform(source.getStatusAsPlatform());
		setDescription(source.getDescription());
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
