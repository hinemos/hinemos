/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;


/**
 * The persistent class for the cc_cfg_facility_relation database table.
 * 
 */
@Entity
@Table(name="cc_cfg_facility_relation", schema="setting")
@Cacheable(true)
@IdClass(FacilityRelationEntityPK.class)
public class FacilityRelationEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String parentFacilityId;
	private String childFacilityId;

	@Deprecated
	public FacilityRelationEntity() {
	}

	public FacilityRelationEntity(String parentFacilityId, String childFacilityId) {
		this.setParentFacilityId(parentFacilityId);
		this.setChildFacilityId(childFacilityId);
	}
	
	@Id
	@Column(name="parent_facility_id")
	public String getParentFacilityId() {
		return this.parentFacilityId;
	}
	public void setParentFacilityId(String parentFacilityId) {
		this.parentFacilityId = parentFacilityId;
	}

	@Id
	@Column(name="child_facility_id")
	public String getChildFacilityId() {
		return this.childFacilityId;
	}
	public void setChildFacilityId(String childFacilityId) {
		this.childFacilityId = childFacilityId;
	}
}