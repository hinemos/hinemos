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
 * The primary key class for the cc_sdml_monitor_notify_relation database table.
 * 
 */
@Embeddable
public class SdmlMonitorNotifyRelationPK implements Serializable {
	private static final long serialVersionUID = 1L;

	private String applicationId;
	private String sdmlMonitorTypeId;

	public SdmlMonitorNotifyRelationPK() {
	}

	public SdmlMonitorNotifyRelationPK(String applicationId, String sdmlMonitorTypeId) {
		this.setApplicationId(applicationId);
		this.setSdmlMonitorTypeId(sdmlMonitorTypeId);
	}

	@Column(name="application_id")
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	@Column(name="sdml_monitor_type_id")
	public String getSdmlMonitorTypeId() {
		return sdmlMonitorTypeId;
	}
	public void setSdmlMonitorTypeId(String sdmlMonitorTypeId) {
		this.sdmlMonitorTypeId = sdmlMonitorTypeId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SdmlMonitorNotifyRelationPK)) {
			return false;
		}
		SdmlMonitorNotifyRelationPK castOther = (SdmlMonitorNotifyRelationPK)other;
		return
				this.applicationId.equals(castOther.applicationId)
				&& this.sdmlMonitorTypeId.equals(castOther.sdmlMonitorTypeId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.applicationId.hashCode();
		hash = hash * prime + this.sdmlMonitorTypeId.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"applicationId",
				"sdmlMonitorTypeId"
		};
		String[] values = {
				this.applicationId,
				this.sdmlMonitorTypeId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}
