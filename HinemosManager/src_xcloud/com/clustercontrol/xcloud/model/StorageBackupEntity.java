/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@NamedQueries({
	@NamedQuery(
			name=StorageBackupEntity.findStorageBackupsByStorageIds,
			query="SELECT i FROM StorageBackupEntity i WHERE i.cloudScopeId = :cloudScopeId AND i.locationId = :locationId AND i.storageId IN :storageIds"
			),
	@NamedQuery(
			name=StorageBackupEntity.findStorageBackups,
			query="SELECT i FROM StorageBackupEntity i WHERE i.cloudScopeId = :cloudScopeId AND i.locationId = :locationId"
			)
})
@Entity
@Table(name="cc_cfg_xcloud_storage_backup", schema="setting")
@IdClass(StorageBackupEntity.StorageBackupEntityPK.class)
public class StorageBackupEntity extends HinemosObjectEntity {
	public final static String findStorageBackupsByStorageIds = "findStorageBackupsByStorageIds";
	public final static String findStorageBackups = "findStorageBackups";

	public static class StorageBackupEntityPK {
		private String cloudScopeId;
		private String locationId;
		private String storageId;
		
		public StorageBackupEntityPK() {
		}
		
		public StorageBackupEntityPK(String cloudScopeId, String locationId, String storageId) {
			this.cloudScopeId = cloudScopeId;
			this.locationId = locationId;
			this.storageId = storageId;
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
	}
	
	private String cloudScopeId;
	private String locationId;
	private String storageId;
	private List<StorageBackupEntryEntity> entries = new ArrayList<>();
	
	private StorageEntity storage;

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
	
	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="storageBackup")
	@JoinColumns({
		@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false),
		@JoinColumn(name="location_id", referencedColumnName="location_id", insertable=false, updatable=false),
		@JoinColumn(name="storage_id", referencedColumnName="storage_id", insertable=false, updatable=false)
	})
	public List<StorageBackupEntryEntity> getEntries() {
		return entries;
	}
	public void setEntries(List<StorageBackupEntryEntity> entries) {
		this.entries = entries;
	}

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false),
		@JoinColumn(name="location_id", referencedColumnName="location_id", insertable=false, updatable=false),
		@JoinColumn(name="storage_id", referencedColumnName="resource_id", insertable=false, updatable=false)
	})
	public StorageEntity getStorage() {
		return storage;
	}
	public void setStorage(StorageEntity storage) {
		this.storage = storage;
	}

	@Override
	public Object getId() {
		return new StorageBackupEntityPK(getCloudScopeId(), getLocationId(), getStorageId());
	}
}
