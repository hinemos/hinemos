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
 * The primary key class for the cc_sdml_control_monitor_relation database table.
 * 
 */
@Embeddable
public class SdmlControlMonitorRelationPK  implements Serializable {
	private static final long serialVersionUID = 1L;

	private String applicationId;
	private String facilityId;
	private String monitorId;

	public SdmlControlMonitorRelationPK() {
	}

	public SdmlControlMonitorRelationPK(String applicationId, String facilityId, String monitorId) {
		this.setApplicationId(applicationId);
		this.setFacilityId(facilityId);
		this.setMonitorId(monitorId);
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

	@Column(name="monitor_id")
	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SdmlControlMonitorRelationPK)) {
			return false;
		}
		SdmlControlMonitorRelationPK castOther = (SdmlControlMonitorRelationPK)other;
		return
				this.applicationId.equals(castOther.applicationId)
				&& this.facilityId.equals(castOther.facilityId)
				&& this.monitorId.equals(castOther.monitorId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.applicationId.hashCode();
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.monitorId.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"applicationId",
				"facilityId",
				"monitorId"
		};
		String[] values = {
				this.applicationId,
				this.facilityId,
				this.monitorId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}
