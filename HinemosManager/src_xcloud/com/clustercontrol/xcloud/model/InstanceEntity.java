/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.MapKey;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.clustercontrol.xcloud.bean.InstanceStatus;
import com.clustercontrol.xcloud.factory.IResourceManagement.Instance.Platform;
import com.clustercontrol.xcloud.persistence.CsvConverter;

/**
 * インスタンス情報のBMP Entity Bean クラス<BR>
 */
@NamedQueries({
	@NamedQuery(
			name=InstanceEntity.findInstancesByLocation,
			query="SELECT i FROM InstanceEntity i WHERE i.cloudScopeId = :cloudScopeId AND i.locationId = :locationId"
			),
	@NamedQuery(
			name=InstanceEntity.findInstancesByInstanceIds,
			query="SELECT i FROM InstanceEntity i WHERE i.cloudScopeId = :cloudScopeId AND i.locationId = :locationId AND i.resourceId IN :instanceIds"
			),
	@NamedQuery(
			name=InstanceEntity.findInstancesByFacilityIds,
			query="SELECT i FROM InstanceEntity i WHERE i.cloudScopeId = :cloudScopeId AND i.facilityId IN :facilityIds order by i.locationId"
			),
	@NamedQuery(
			name=InstanceEntity.findParentsOfFacility,
			query="SELECT DISTINCT f.facilityId FROM FacilityInfo f, FacilityRelationEntity r WHERE r.childFacilityId = :facilityId AND f.facilityId = r.parentFacilityId"
	)
})
@Entity
@Table(name="cc_cfg_xcloud_instance", schema="setting")
@DiscriminatorValue(InstanceEntity.typeName)
public class InstanceEntity extends LocationResourceEntity implements IAssignableEntity {
	public static final String typeName = "instance";
	
	public static final String findInstancesByLocation = "findInstancesByLocation";
	public static final String findInstancesByInstanceIds = "findInstancesByInstanceIds";
	public static final String findInstancesByFacilityIds = "findInstancesByFacilityIds";
	public static final String findParentsOfFacility = "findParentsOfFacility";
	
	private String name;
	private String facilityId;
	private InstanceStatus instanceStatus;
	private String instanceStatusAsPlatform;
	private Platform platform;
	private String resourceTypeAsPlatform;
	private List<String> ipAddresses = new ArrayList<>();
	private String memo;
	private Map<String, ResourceTag> tags = new HashMap<>();
	
	private InstanceBackupEntity backup;

	public InstanceEntity() {
	}

	@Column(name="instance_name")
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Column(name="facility_id", unique=true)
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId( String facilityId ) {
		this.facilityId = facilityId;
	}

	@Column(name="instance_status")
	@Enumerated(EnumType.STRING)
	public InstanceStatus getInstanceStatus() {
		return this.instanceStatus;
	}
	public void setInstanceStatus(InstanceStatus instanceStatus) {
		this.instanceStatus = instanceStatus;
	}

	@Column(name="platform")
	@Enumerated(EnumType.STRING)
	public Platform getPlatform() {
		return platform;
	}
	public void setPlatform(Platform platform) {
		this.platform = platform;
	}
	
	@Column(name="instance_status_as_platform")
	public String getInstanceStatusAsPlatform() {
		return instanceStatusAsPlatform;
	}
	public void setInstanceStatusAsPlatform(String instanceStatusAsPlatform) {
		this.instanceStatusAsPlatform = instanceStatusAsPlatform;
	}

	@Column(name="resource_type_as_platform")
	public String getResourceTypeAsPlatform() {
		return resourceTypeAsPlatform;
	}
	public void setResourceTypeAsPlatform(String resourceTypeAsPlatform) {
		this.resourceTypeAsPlatform = resourceTypeAsPlatform;
	}
	
	@Column(name="ip_addresses")
	@Convert(converter=CsvConverter.class)
	public List<String> getIpAddresses() {
		return this.ipAddresses;
	}
	public void setIpAddresses(List<String> ipAddresses) {
		this.ipAddresses = ipAddresses;
	}
	
	@ElementCollection
	@CollectionTable(
		name="cc_cfg_xcloud_instance_tag", schema="setting",
		joinColumns={@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id"),
				@JoinColumn(name="location_id", referencedColumnName="location_id"),
				@JoinColumn(name="instance_id", referencedColumnName="resource_id")}
	)
	@MapKey(name="key")
	public Map<String, ResourceTag> getTags() {
		return tags;
	}
	public void setTags(Map<String, ResourceTag> tags) {
		this.tags = tags;
	}
	
	@OneToOne(fetch=FetchType.EAGER)
	@JoinColumns({
		@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false),
		@JoinColumn(name="location_id", referencedColumnName="location_id", insertable=false, updatable=false),
		@JoinColumn(name="resource_id", referencedColumnName="instance_id", insertable=false, updatable=false)
	})
	public InstanceBackupEntity getBackup() {
		return this.backup;
	}
	public void setBackup(InstanceBackupEntity backup) {
		this.backup = backup;
	}
	
	@Column(name="memo")
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}

	@Override
	public String toString() {
		return "InstanceEntity ["
				+ "name=" + name
				+ ", facilityId=" + facilityId
				+ ", instanceStatus=" + instanceStatus
				+ ", instanceStatusAsPlatform=" + instanceStatusAsPlatform
				+ ", platform=" + platform
				+ ", resourceTypeAsPlatform=" + resourceTypeAsPlatform
				+ ", ipAddresses=" + ipAddresses
				+ ", memo=" + memo
				+ ", tags=" + tags
				+ ", backup=" + backup
				+ "]";
	}
}
