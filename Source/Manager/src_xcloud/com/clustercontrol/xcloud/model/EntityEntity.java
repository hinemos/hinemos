/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * インスタンス情報のBMP Entity Bean クラス<BR>
 */
@NamedQueries({
	@NamedQuery(
			name=EntityEntity.findEntitiesByLocation,
			query="SELECT e FROM EntityEntity e WHERE e.cloudScopeId = :cloudScopeId AND e.locationId = :locationId"
			),
	@NamedQuery(
			name=EntityEntity.findEntitiesByFacilityIds,
			query="SELECT e FROM EntityEntity e WHERE e.cloudScopeId = :cloudScopeId AND e.facilityId IN :facilityIds order by e.locationId"
			),
	@NamedQuery(
			name=EntityEntity.findEntitiesByEntityIds,
			query="SELECT e FROM EntityEntity e WHERE e.cloudScopeId = :cloudScopeId AND e.locationId = :locationId AND e.resourceId IN :entityIds"
			),
	@NamedQuery(
			name=EntityEntity.findEntityByPlatformEntityIds,
			query="SELECT e FROM EntityEntity e WHERE e.cloudScopeId = :cloudScopeId AND e.locationId = :locationId AND e.platformEntityId IN :platformEntityIds"
			)
})
@Entity
@Table(name="cc_cfg_xcloud_entity", schema="setting")
@DiscriminatorValue(EntityEntity.typeName)
public class EntityEntity extends LocationResourceEntity implements IAssignableEntity {
	public static final String typeName = "entity";
	
	public static final String findEntitiesByLocation = "findEntitiesByLocation";
	public static final String findEntitiesByFacilityIds = "findEntitiesByFacilityIds";
	public static final String findEntitiesByEntityIds = "findEntitiesByEntityIds";
	public static final String findEntityByPlatformEntityIds = "findEntityByPlatformEntityIds";
	
	private String platformEntityId;
	private String entityType;
	private String name;
	private String facilityId;

	private Map<String, ResourceTag> tags = new HashMap<>();
	
	public EntityEntity() {
	}

	@Column(name="platform_entity_id")
	public String getPlatformEntityId() {
		return platformEntityId;
	}
	public void setPlatformEntityId(String platformEntityId) {
		this.platformEntityId = platformEntityId;
	}

	@Column(name="entity_type")
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	@Override
	@Column(name="entity_name")
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	@Column(name="facility_id", unique=true)
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId( String facilityId ) {
		this.facilityId = facilityId;
	}
	
	@Override
	@Transient
	public Map<String, ResourceTag> getTags() {
		return tags;
	}
	public void setTags(Map<String, ResourceTag> tags) {
		this.tags = tags;
	}

	@Override
	public List<String> getIpAddresses() {
		return Collections.emptyList();
	}
	
	@Override
	public boolean isPatternEnabled() {
		return false;
	}
}
