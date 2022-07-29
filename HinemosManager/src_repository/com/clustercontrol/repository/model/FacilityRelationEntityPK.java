/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * The primary key class for the cc_cfg_facility_relation database table.
 * 
 */
//@Embeddable
public class FacilityRelationEntityPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String parentFacilityId;
	private String childFacilityId;

	public FacilityRelationEntityPK() {
	}

	public FacilityRelationEntityPK(String parentFacilityId, String childFacilityId) {
		this.setParentFacilityId(parentFacilityId);
		this.setChildFacilityId(childFacilityId);
	}

//	@Column(name="parent_facility_id")
	public String getParentFacilityId() {
		return this.parentFacilityId;
	}
	public void setParentFacilityId(String parentFacilityId) {
		this.parentFacilityId = parentFacilityId;
	}

//	@Column(name="child_facility_id")
	public String getChildFacilityId() {
		return this.childFacilityId;
	}
	public void setChildFacilityId(String childFacilityId) {
		this.childFacilityId = childFacilityId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof FacilityRelationEntityPK)) {
			return false;
		}
		FacilityRelationEntityPK castOther = (FacilityRelationEntityPK)other;
		return
			this.parentFacilityId.equals(castOther.parentFacilityId)
			&& this.childFacilityId.equals(castOther.childFacilityId);
	}

	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.parentFacilityId.hashCode();
		hash = hash * prime + this.childFacilityId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"parentFacilityId",
				"childFacilityId"
		};
		String[] values = {
				this.parentFacilityId,
				this.childFacilityId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}