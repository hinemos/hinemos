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

import javax.persistence.*;

/**
 * The primary key class for the cc_cfg_node_variable database table.
 * 
 */
@Embeddable
public class NodeVariableInfoPK implements Serializable, Cloneable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private String nodeVariableName;

	public NodeVariableInfoPK() {
	}

	public NodeVariableInfoPK(String facilityId, String nodeVariableName) {
		this.setFacilityId(facilityId);
		this.setNodeVariableName(nodeVariableName);
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="node_variable_name")
	public String getNodeVariableName() {
		return this.nodeVariableName;
	}
	public void setNodeVariableName(String nodeVariableName) {
		this.nodeVariableName = nodeVariableName;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NodeVariableInfoPK)) {
			return false;
		}
		NodeVariableInfoPK castOther = (NodeVariableInfoPK)other;
		return
				this.facilityId.equals(castOther.facilityId)
				&& this.nodeVariableName.equals(castOther.nodeVariableName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.facilityId.hashCode();
		hash = hash * prime + this.nodeVariableName.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		String[] names = {
				"facilityId",
				"nodeVariableName"
		};
		String[] values = {
				this.facilityId,
				this.nodeVariableName
		};
		return Arrays.toString(names) + " = " + Arrays.toString(values);
	}
	
	@Override
	public NodeVariableInfoPK clone() {
		try {
			NodeVariableInfoPK cloneInfo = (NodeVariableInfoPK)super.clone();
			cloneInfo.facilityId = this.facilityId;
			cloneInfo.nodeVariableName = this.nodeVariableName;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}