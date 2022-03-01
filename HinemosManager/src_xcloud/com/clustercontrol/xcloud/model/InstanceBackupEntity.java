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
			name=InstanceBackupEntity.findInstanceBackupsByInstanceIds,
			query="SELECT i FROM InstanceBackupEntity i WHERE i.cloudScopeId = :cloudScopeId AND i.locationId = :locationId AND i.instanceId IN :instanceIds"
			)
})
@Entity
@Table(name="cc_cfg_xcloud_instance_backup", schema="setting")
@IdClass(InstanceBackupEntity.InstanceBackupEntityPK.class)
public class InstanceBackupEntity extends HinemosObjectEntity {
	public final static String findInstanceBackupsByInstanceIds = "findInstanceBackupsByInstanceIds";
	
	public static class InstanceBackupEntityPK {
		private String cloudScopeId;
		private String locationId;
		private String instanceId;
		
		public InstanceBackupEntityPK() {
		}
		
		public InstanceBackupEntityPK(String cloudScopeId, String locationId, String instanceId) {
			this.cloudScopeId = cloudScopeId;
			this.locationId = locationId;
			this.instanceId = instanceId;
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
	}
	
	private String cloudScopeId;
	private String locationId;
	private String instanceId;
	private List<InstanceBackupEntryEntity> entries = new ArrayList<>();
	
	private InstanceEntity instance;

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
	
	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="instanceBackup")
	@JoinColumns({
		@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false),
		@JoinColumn(name="location_id", referencedColumnName="location_id", insertable=false, updatable=false),
		@JoinColumn(name="instance_id", referencedColumnName="instance_id", insertable=false, updatable=false)
	})
	public List<InstanceBackupEntryEntity> getEntries() {
		return entries;
	}
	public void setEntries(List<InstanceBackupEntryEntity> entries) {
		this.entries = entries;
	}

	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false),
		@JoinColumn(name="location_id", referencedColumnName="location_id", insertable=false, updatable=false),
		@JoinColumn(name="instance_id", referencedColumnName="resource_id", insertable=false, updatable=false)
	})
	public InstanceEntity getInstance() {
		return instance;
	}
	public void setInstance(InstanceEntity instance) {
		this.instance = instance;
	}

	@Override
	public Object getId() {
		return new InstanceBackupEntityPK(getCloudScopeId(), getLocationId(), getInstanceId());
	}
}
