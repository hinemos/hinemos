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

import jakarta.persistence.*;

/**
 * The primary key class for the cc_cfg_node_license database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@Embeddable
public class NodeLicenseInfoPK implements Serializable, Cloneable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private String productName;

	public NodeLicenseInfoPK() {
	}

	public NodeLicenseInfoPK(String facilityId, String productName) {
		this.setFacilityId(facilityId);
		this.setProductName(productName);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="product_name")
	public String getProductName() {
		return this.productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NodeLicenseInfoPK)) {
			return false;
		}
		NodeLicenseInfoPK castOther = (NodeLicenseInfoPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.productName.equals(castOther.productName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.productName.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"productName"
		};
		String[] values = {
				this.facilityId,
				this.productName
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
	
	@Override
	public NodeLicenseInfoPK clone() {
		try {
			NodeLicenseInfoPK cloneInfo = (NodeLicenseInfoPK)super.clone();
			cloneInfo.facilityId = this.facilityId;
			cloneInfo.productName = this.productName;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}