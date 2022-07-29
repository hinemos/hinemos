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

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKey;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import com.clustercontrol.xcloud.factory.IResourceManagement.InstanceSnapshot.InstanceSnapshotStatusType;
import com.clustercontrol.xcloud.persistence.ApplyCurrentTime;
import com.clustercontrol.xcloud.persistence.ApplyUserName;

@NamedQueries({
	@NamedQuery(
			name=InstanceBackupEntryEntity.findInstanceBackupEntriesByEntryIds,
			query="SELECT i FROM InstanceBackupEntryEntity i WHERE i.cloudScopeId = :cloudScopeId AND i.locationId = :locationId AND i.entryId IN :entryIds ORDER BY i.entryId"
			)
})
@Entity
@Table(name="cc_cfg_xcloud_instance_backup_entry", schema="setting")
@IdClass(InstanceBackupEntryEntity.InstanceBackupEntryEntityPK.class)
public class InstanceBackupEntryEntity extends CloudObjectEntity {
	public static final String findInstanceBackupEntriesByEntryIds = "findInstanceBackupEntriesByEntryIds";
	
	public static class InstanceBackupEntryEntityPK {
		private String cloudScopeId;
		private String locationId;
		private String instanceId;
		private String entryId;
		
		public InstanceBackupEntryEntityPK() {
		}
		
		public InstanceBackupEntryEntityPK(String cloudScopeId, String locationId, String instanceId, String entryId) {
			this.cloudScopeId = cloudScopeId;
			this.locationId = locationId;
			this.instanceId = instanceId;
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
		
		public String getInstanceId() {
			return instanceId;
		}
		public void setInstanceId(String instanceId) {
			this.instanceId = instanceId;
		}

		public String getEntryId() {
			return entryId;
		}
		public void setEntryId(String entryId) {
			this.entryId = entryId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((cloudScopeId == null) ? 0 : cloudScopeId.hashCode());
			result = prime * result + ((entryId == null) ? 0 : entryId.hashCode());
			result = prime * result + ((instanceId == null) ? 0 : instanceId.hashCode());
			result = prime * result + ((locationId == null) ? 0 : locationId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InstanceBackupEntryEntityPK other = (InstanceBackupEntryEntityPK) obj;
			if (cloudScopeId == null) {
				if (other.cloudScopeId != null)
					return false;
			} else if (!cloudScopeId.equals(other.cloudScopeId))
				return false;
			if (entryId == null) {
				if (other.entryId != null)
					return false;
			} else if (!entryId.equals(other.entryId))
				return false;
			if (instanceId == null) {
				if (other.instanceId != null)
					return false;
			} else if (!instanceId.equals(other.instanceId))
				return false;
			if (locationId == null) {
				if (other.locationId != null)
					return false;
			} else if (!locationId.equals(other.locationId))
				return false;
			return true;
		}
	}
	
	private String cloudScopeId;
	private String locationId;
	private String instanceId;
	private String entryId;
	
	private String name;
	private String description;
	
	private InstanceSnapshotStatusType status;
	private String statusAsPlatform;
	
	private Long createTime;

	private Long regDate;
	private String regUser;
	
	private InstanceBackupEntity instanceBackup;
	
	private Map<String, BackupedData> backupedData = new HashMap<>();
	
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
	@Column(name="instance_id")
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
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
	public InstanceSnapshotStatusType getStatus() {
		return status;
	}
	public void setStatus(InstanceSnapshotStatusType status) {
		this.status = status;
	}
	
	@Column(name="status_detail")
	public String getStatusAsPlatform() {
		return statusAsPlatform;
	}
	public void setStatusAsPlatform(String statusAsPlatform) {
		this.statusAsPlatform = statusAsPlatform;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false),
		@JoinColumn(name="location_id", referencedColumnName="location_id", insertable=false, updatable=false),
		@JoinColumn(name="instance_id", referencedColumnName="instance_id", insertable=false, updatable=false)
	})
	public InstanceBackupEntity getInstanceBackup() {
		return instanceBackup;
	}
	public void setInstanceBackup(InstanceBackupEntity instanceBackup) {
		this.instanceBackup = instanceBackup;
	}
	
	@ElementCollection
	@CollectionTable(
		name="cc_cfg_xcloud_instance_backup_data", schema="setting",
		joinColumns={
				@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id"),
				@JoinColumn(name="location_id", referencedColumnName="location_id"),
				@JoinColumn(name="instance_id", referencedColumnName="instance_id"),
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
	
	@Override
	public InstanceBackupEntryEntityPK getId() {
		return new InstanceBackupEntryEntityPK(getCloudScopeId(), getLocationId(), getInstanceId(), getEntryId());
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
