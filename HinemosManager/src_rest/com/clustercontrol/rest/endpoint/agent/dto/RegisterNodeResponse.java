/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.repository.bean.AutoRegisterStatus;

public class RegisterNodeResponse {

	private String facilityId;
	private AutoRegisterStatus resultStatus;

	public RegisterNodeResponse() {
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public AutoRegisterStatus getResultStatus() {
		return resultStatus;
	}

	public void setResultStatus(AutoRegisterStatus resultStatus) {
		this.resultStatus = resultStatus;
	}

}
