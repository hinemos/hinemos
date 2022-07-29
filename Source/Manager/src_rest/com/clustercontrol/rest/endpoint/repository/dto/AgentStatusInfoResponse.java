/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.AgentUpdateStatusEnum;

public class AgentStatusInfoResponse {

	private String facilityId;
	private String facilityName;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String startupTime;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String lastLogin;
	private String multiplicity;
	@RestBeanConvertEnum
	private AgentUpdateStatusEnum updateStatusCode;

	public AgentStatusInfoResponse() {
	}

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

	public String getStartupTime() {
		return startupTime;
	}

	public void setStartupTime(String startupTime) {
		this.startupTime = startupTime;
	}

	public String getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(String lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getMultiplicity() {
		return multiplicity;
	}

	public void setMultiplicity(String multiplicity) {
		this.multiplicity = multiplicity;
	}

	public AgentUpdateStatusEnum getUpdateStatusCode() {
		return updateStatusCode;
	}

	public void setUpdateStatusCode(AgentUpdateStatusEnum updateStatusCode) {
		this.updateStatusCode = updateStatusCode;
	}
}
