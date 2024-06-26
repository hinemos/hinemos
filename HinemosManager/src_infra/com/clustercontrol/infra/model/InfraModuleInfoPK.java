/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;


/**
 * The primary key class for the cc_monitor_trap_value_info database table.
 * 
 */
@Embeddable
public class InfraModuleInfoPK implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String managementId;
	private String moduleId;
	
	public InfraModuleInfoPK() {
	}

	public InfraModuleInfoPK(String managementId, String moduleId) {
		this.setManagementId(managementId);
		this.setModuleId(moduleId);
	}
	
	@Column(name="management_id")
	public String getManagementId() {
		return managementId;
	}
	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}
	
	@Column(name="module_id")
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	@Override
	public String toString() {
		return "InfraModuleInfoEntityPK [managementId=" + managementId
				+ ", moduleId=" + moduleId + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((managementId == null) ? 0 : managementId.hashCode());
		result = prime * result
				+ ((moduleId == null) ? 0 : moduleId.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InfraModuleInfoPK other = (InfraModuleInfoPK) obj;
		if (managementId == null) {
			if (other.managementId != null)
				return false;
		} else if (!managementId.equals(other.managementId))
			return false;
		if (moduleId == null) {
			if (other.moduleId != null)
				return false;
		} else if (!moduleId.equals(other.moduleId))
			return false;
		return true;
	}
}
