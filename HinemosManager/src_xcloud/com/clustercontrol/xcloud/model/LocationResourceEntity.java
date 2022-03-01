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

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKey;
import jakarta.persistence.Table;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;

@Entity
@Table(name="cc_cfg_xcloud_location_resource", schema="setting")
@DiscriminatorColumn(name="resource_type")
@Inheritance(strategy=InheritanceType.JOINED)
@IdClass(LocationResourceEntity.LocationResourceEntityPK.class)
public abstract class LocationResourceEntity extends HinemosObjectEntity {
	public static class LocationResourceEntityPK {
		private String cloudScopeId;
		private String locationId;
		private String resourceId;
		
		public LocationResourceEntityPK() {
		}
		
		public LocationResourceEntityPK(String cloudScopeId, String locationId, String resourceId) {
			this.cloudScopeId = cloudScopeId;
			this.locationId = locationId;
			this.resourceId = resourceId;
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
		
		public String getResourceId() {
			return resourceId;
		}
		public void setResourceId(String resourceId) {
			this.resourceId = resourceId;
		}
	}
	
	private String cloudScopeId;
	private String locationId;
	private String resourceId;
	private String resourceType;
	
	private List<FacilityAdditionEntity> facilities = new ArrayList<>();
	
	private Map<String, ExtendedProperty> extendedProperties = new HashMap<>();
	
	private CloudScopeEntity cloudScope;

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
	@Column(name="resource_id")
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	@Column(name="resource_type")
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
	@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false)
	public CloudScopeEntity getCloudScope() {
		return cloudScope;
	}
	public void setCloudScope(CloudScopeEntity cloudScope) {
		this.cloudScope = cloudScope;
	}
	
	@ManyToMany(mappedBy="resources")
	public List<FacilityAdditionEntity> getFacilities() {
		return facilities;
	}
	public void setFacilities(List<FacilityAdditionEntity> facilities) {
		this.facilities = facilities;
	}
	
	@ElementCollection
	@CollectionTable(
		name="cc_cfg_xcloud_location_resource_eprop", schema="setting",
		joinColumns={
				@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id"),
				@JoinColumn(name="location_id", referencedColumnName="location_id"),
				@JoinColumn(name="resource_id", referencedColumnName="resource_id")}
	)
	@MapKey(name="name")
	@AttributeOverrides({
		@AttributeOverride(name="name", column=@Column(name="name")),
		@AttributeOverride(name="value", column=@Column(name="value"))
	})
	public Map<String, ExtendedProperty> getExtendedProperties() {
		return extendedProperties;
	}
	public void setExtendedProperties(Map<String, ExtendedProperty> extendedProperties) {
		this.extendedProperties = extendedProperties;
	}
	
	public void putExtendeProperty(String name, DataType type, String value) {
		ExtendedProperty property = extendedProperties.get(name);
		if (property == null) {
			property = new ExtendedProperty();
			property.setName(name);
			property.setDataType(type);
			property.setValue(value);
			
			HinemosEntityManager em = Session.current().getEntityManager();
			PersistenceUtil.persist(em, property);
			
			extendedProperties.put(name, property);
		} else {
			property.setDataType(type);
			property.setValue(value);
		}
	}
	
	@Override
	public LocationResourceEntity.LocationResourceEntityPK getId() {
		return new LocationResourceEntity.LocationResourceEntityPK(getCloudScopeId(), getLocationId(), getResourceId());
	}
}
