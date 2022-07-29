/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.rest.endpoint.infra.dto.enumtype.OkNgEnum;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.RunCheckTypeEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;

public class ModuleNodeResultResponseP1 {

	@RestBeanConvertEnum
	private RunCheckTypeEnum runCheckType;
	private String facilityId;
	@RestBeanConvertEnum
	private OkNgEnum result;
	private String message;

	public ModuleNodeResultResponseP1() {
	}

	public RunCheckTypeEnum getRunCheckType() {
		return runCheckType;
	}

	public void setRunCheckType(RunCheckTypeEnum runCheckType) {
		this.runCheckType = runCheckType;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public OkNgEnum getResult() {
		return result;
	}

	public void setResult(OkNgEnum result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "ModuleNodeResultResponseP1 [runCheckType=" + runCheckType + ", facilityId=" + facilityId + ", result="
				+ result + ", message=" + message + "]";
	}

}
