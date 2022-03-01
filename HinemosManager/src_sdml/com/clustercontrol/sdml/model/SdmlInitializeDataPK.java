/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.model;

import java.io.Serializable;
import java.util.Arrays;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * The primary key class for the cc_sdml_initialize_data database table.
 * 
 */
@Embeddable
public class SdmlInitializeDataPK implements Serializable {
	private static final long serialVersionUID = 1L;

	private String applicationId;
	private String facilityId;
	private String key;

	public SdmlInitializeDataPK() {
	}

	public SdmlInitializeDataPK(String applicationId, String facilityId, String key) {
		this.setApplicationId(applicationId);
		this.setFacilityId(facilityId);
		this.setKey(key);
	}

	@Column(name="application_id")
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="key")
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SdmlInitializeDataPK)) {
			return false;
		}
		SdmlInitializeDataPK castOther = (SdmlInitializeDataPK)other;
		return
				this.applicationId.equals(castOther.applicationId)
				&& this.facilityId.equals(castOther.facilityId)
				&& this.key.equals(castOther.key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.applicationId.hashCode();
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.key.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"applicationId",
				"facilityId",
				"key"
		};
		String[] values = {
				this.applicationId,
				this.facilityId,
				this.key
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}
