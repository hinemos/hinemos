/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.clustercontrol.xcloud.factory.IResourceManagement.StorageSnapshot.StorageSnapshotStatusType;
import com.clustercontrol.xcloud.persistence.ApplyCurrentTime;
import com.clustercontrol.xcloud.persistence.ApplyUserName;

@NamedQueries({
	@NamedQuery(
			name=StorageBackupEntryEntity.findStorageBackupEntriesByEntryIds,
			query="SELECT i FROM StorageBackupEntryEntity i WHERE i.cloudScopeId = :cloudScopeId AND i.locationId = :locationId AND i.entryId IN :entryIds ORDER BY i.entryId"
			),
	@NamedQuery(
			name=StorageBackupEntryEntity.findStorageBackupEntriesByLocation,
			query="SELECT i FROM StorageBackupEntryEntity i WHERE i.cloudScopeId = :cloudScopeId AND i.locationId = :locationId"
			)
})
@Entity
@Table(name="cc_cfg_xcloud_storage_backup_entry", schema="setting")
@IdClass(StorageBackupEntryEntity.StorageBackupEntryEntityPK.class)
public class StorageBackupEntryEntity extends CloudObjectEntity {
	public static final String findStorageBackupEntriesByEntryIds = "findStorageBackupEntriesByEntryIds";
	public static final String findStorageBackupEntriesByLocation = "findStorageBackupEntriesByLocation";
	
	public static class StorageBackupEntryEntityPK {
		private String cloudScopeId;
		private String locationId;
		private String storageId;
		private String entryId;
		
		public StorageBackupEntryEntityPK() {
		}
		
		public StorageBackupEntryEntityPK(String cloudScopeId, String locationId, String storageId, String entryId) {
			this.cloudScopeId = cloudScopeId;
			this.locationId = locationId;
			this.storageId = storageId;
			this.setEntryId(entryId);
		}
		
		public String getCloudScopeId() {
			return cloudScopeId;
		}
		public void setCloudScopeId(String cloudScopeId) {
			this.cloudScopeId = cloudScopeId;
		}
		
		public String getLocationId() {
			return this.locationId;
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

		public String getEntryId() {
			return entryId;
		}
		public void setEntryId(String entryId) {
			this.entryId = entryId;
		}
	}
	
	private String cloudScopeId;
	private String locationId;
	private String storageId;
	private String entryId;
	
	private String name;
	private String description;
	
	private StorageSnapshotStatusType status;
	private String statusAsPlatform;
	
	private Long createTime;

	private Long regDate;
	private String regUser;
	
	private Map<String, BackupedData> backupedData = new HashMap<>();
	
	private StorageBackupEntity storageBackup;
	
	@Id
	@Column(name="cloud_scope_id")
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	@Id
	@Column(name="location_id")
	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	@Id
	@Column(name="storage_id")
	public String getStorageId() {
		return storageId;
	}
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}
	@Id
	@Column(name="entry_id")
	public String getEntryId() {
		return entryId;
	}
	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}
	
	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(name="status")
	@Enumerated(EnumType.STRING)
	public StorageSnapshotStatusType getStatus() {
		return status;
	}
	public void setStatus(StorageSnapshotStatusType status) {
		this.status = status;
	}
	
	@Column(name="status_detail")
	public String getStatusAsPlatform() {
		return statusAsPlatform;
	}
	public void setStatusAsPlatform(String statusAsPlatform) {
		this.statusAsPlatform = statusAsPlatform;
	}

	@ElementCollection
	@CollectionTable(
		name="cc_cfg_xcloud_storage_backup_data", schema="setting",
		joinColumns={
				@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id"),
				@JoinColumn(name="location_id", referencedColumnName="location_id"),
				@JoinColumn(name="storage_id", referencedColumnName="storage_id"),
				@JoinColumn(name="entry_id", referencedColumnName="entry_id")}
	)
	@MapKey(name="name")
	@AttributeOverrides({
		@AttributeOverride(name="name", column=@Column(name="name")),
		@AttributeOverride(name="value", column=@Column(name="value"))
	})
	public Map<String, BackupedData> getBackupedData() {
		return backupedData;
	}
	public void setBackupedData(Map<String, BackupedData> backupedData) {
		this.backupedData = backupedData;
	}
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false),
		@JoinColumn(name="location_id", referencedColumnName="location_id", insertable=false, updatable=false),
		@JoinColumn(name="storage_id", referencedColumnName="storage_id", insertable=false, updatable=false)
	})
	public StorageBackupEntity getStorageBackup() {
		return storageBackup;
	}
	public void setStorageBackup(StorageBackupEntity storageBackup) {
		this.storageBackup = storageBackup;
	}
	
	@Override
	public Object getId() {
		return new StorageBackupEntryEntityPK(getCloudScopeId(), getLocationId(), getStorageId(), getEntryId());
	}
	
	@Column(name="create_time")
	public Long getCreateTime() {
		return this.createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	@Column(name="reg_date")
	@ApplyCurrentTime(onlyPersist=true)
	public Long getRegDate() {
		return this.regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	@Column(name="reg_user")
	@ApplyUserName(onlyPersist=true)
	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(java.lang.String regUser) {
		this.regUser = regUser;
	}
}
