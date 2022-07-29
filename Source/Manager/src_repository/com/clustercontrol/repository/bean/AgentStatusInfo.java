/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class AgentStatusInfo  implements Serializable{

	private static final long serialVersionUID = -5818466250922407514L;
	private String facilityId = "";
	private String facilityName = "";
	private Long startupTime = 0l;
	private Long lastLogin = 0l;
	private String multiplicity = "";
	private AgentUpdateStatus updateStatus;

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public Long getStartupTime() {
		return startupTime;
	}

	public void setStartupTime(Long startupTime) {
		this.startupTime = startupTime;
	}

	public Long getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Long lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getMultiplicity() {
		return multiplicity;
	}

	public void setMultiplicity(String multiplicity) {
		this.multiplicity = multiplicity;
	}

	public AgentUpdateStatus getUpdateStatus() {
		return updateStatus;
	}

	public void setUpdateStatus(AgentUpdateStatus status) {
		this.updateStatus = status;
	}
}
