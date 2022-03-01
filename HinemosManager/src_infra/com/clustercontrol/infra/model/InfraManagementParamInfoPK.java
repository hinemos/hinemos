/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.model;

import java.io.Serializable;
import java.util.Arrays;

import jakarta.persistence.*;

/**
 * The primary key class for the cc_infra_management_param_info database table.
 * 
 */
@Embeddable
public class InfraManagementParamInfoPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String managementId;
	private String paramId;

	public InfraManagementParamInfoPK() {
	}

	public InfraManagementParamInfoPK(String managementId, String paramId) {
		this.setManagementId(managementId);
		this.setParamId(paramId);
	}

	@Column(name="management_id")
	public String getManagementId() {
		return this.managementId;
	}
	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}

	@Column(name="param_id")
	public String getParamId() {
		return this.paramId;
	}
	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (!(other instanceof InfraManagementParamInfoPK)) {
			return false;
		}
		InfraManagementParamInfoPK castOther = (InfraManagementParamInfoPK)other;
		return
				this.managementId.equals(castOther.managementId)
				&& this.paramId.equals(castOther.paramId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.managementId.hashCode();
		hash = hash * prime + this.paramId.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"managementId",
				"paramId"
		};
		String[] values = {
				this.managementId,
				this.paramId
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
}