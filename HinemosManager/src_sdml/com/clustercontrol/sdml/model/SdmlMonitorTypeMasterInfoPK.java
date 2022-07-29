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
 * The primary key class for the cc_sdml_monitor_type_mst database table.
 * 
 */
@Embeddable
public class SdmlMonitorTypeMasterInfoPK implements Serializable {
	private static final long serialVersionUID = 1L;

	private String sdmlMonitorTypeId;

	public SdmlMonitorTypeMasterInfoPK() {
	}

	public SdmlMonitorTypeMasterInfoPK(String sdmlMonitorTypeId) {
		this.setSdmlMonitorTypeId(sdmlMonitorTypeId);
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
		if (!(other instanceof SdmlMonitorTypeMasterInfoPK)) {
			return false;
		}
		SdmlMonitorTypeMasterInfoPK castOther = (SdmlMonitorTypeMasterInfoPK)other;
		return
				this.sdmlMonitorTypeId.equals(castOther.sdmlMonitorTypeId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.sdmlMonitorTypeId.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"sdmlMonitorTypeId"
		};
		String[] values = {
				this.sdmlMonitorTypeId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}
