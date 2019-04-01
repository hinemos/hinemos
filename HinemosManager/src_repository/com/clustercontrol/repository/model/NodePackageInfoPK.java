/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.*;

/**
 * The primary key class for the cc_cfg_node_package database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodePackageInfoPK implements Serializable, Cloneable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private String packageId;

	public NodePackageInfoPK() {
	}

	public NodePackageInfoPK(String facilityId, String packageId) {
		this.setFacilityId(facilityId);
		this.setPackageId(packageId);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="package_id")
	public String getPackageId() {
		return this.packageId;
	}
	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NodePackageInfoPK)) {
			return false;
		}
		NodePackageInfoPK castOther = (NodePackageInfoPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.packageId.equals(castOther.packageId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.packageId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"packageId"
		};
		String[] values = {
				this.facilityId,
				this.packageId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
	
	@Override
	public NodePackageInfoPK clone() {
		try {
			NodePackageInfoPK cloneInfo = (NodePackageInfoPK)super.clone();
			cloneInfo.facilityId = this.facilityId;
			cloneInfo.packageId = this.packageId;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}