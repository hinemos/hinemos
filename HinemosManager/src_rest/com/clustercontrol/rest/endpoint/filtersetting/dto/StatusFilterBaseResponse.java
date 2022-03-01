/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import java.util.List;

import com.clustercontrol.filtersetting.bean.FacilityTarget;
import com.clustercontrol.filtersetting.bean.StatusFilterBaseInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = StatusFilterBaseInfo.class)
public class StatusFilterBaseResponse {

	private String facilityId;
	private FacilityTarget facilityTarget;
	private List<StatusFilterConditionResponse> conditions;

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public FacilityTarget getFacilityTarget() {
		return facilityTarget;
	}

	public void setFacilityTarget(FacilityTarget facilityTarget) {
		this.facilityTarget = facilityTarget;
	}

	public List<StatusFilterConditionResponse> getConditions() {
		return conditions;
	}

	public void setConditions(List<StatusFilterConditionResponse> conditions) {
		this.conditions = conditions;
	}

}
