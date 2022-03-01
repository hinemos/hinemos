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
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKey;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@NamedQueries({
	@NamedQuery(
			name=FacilityAdditionEntity.findParentFacilityAdditionsOfFacility,
			query="SELECT a FROM FacilityAdditionEntity a JOIN FacilityRelationEntity r ON a.facilityId = r.parentFacilityId WHERE r.childFacilityId = :facilityId"
			),
	@NamedQuery(
			name=FacilityAdditionEntity.findFacilityAdditions,
			query="SELECT a FROM FacilityAdditionEntity a WHERE a.facilityId IN :facilityIds"
			)
})
@Entity
@Table(name="cc_cfg_xcloud_facility_addtion", schema="setting")
public class FacilityAdditionEntity {
	public final static String findParentFacilityAdditionsOfFacility = "findParentFacilityAdditionsOfFacility";
	public final static String findFacilityAdditions = "findFacilityAdditions";
	
	private String facilityId;
	private String cloudScopeId;
	private String type;
	private List<LocationResourceEntity> resources = new ArrayList<>();
	private Map<String, ExtendedProperty> extendedProperties = new HashMap<>();

	@Id
	@Column(name="facility_id")
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="cloud_scope_id")
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}
	
	@Column(name="type")
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	@ElementCollection
	@CollectionTable(
		name="cc_cfg_xcloud_facility_addtion_eprop", schema="setting",
		joinColumns=@JoinColumn(name="facility_id")
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
	
	@ManyToMany
	@JoinTable(
		name="cc_cfg_xcloud_facility_location_resource", schema="setting",
		joinColumns=@JoinColumn(name="facility_id"),
		inverseJoinColumns={@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id"), @JoinColumn(name="location_id", referencedColumnName="location_id"), @JoinColumn(name="resource_id", referencedColumnName="resource_id")}
		)
	public List<LocationResourceEntity> getResources() {
		return resources;
	}
	public void setResources(List<LocationResourceEntity> resources) {
		this.resources = resources;
	}
}
