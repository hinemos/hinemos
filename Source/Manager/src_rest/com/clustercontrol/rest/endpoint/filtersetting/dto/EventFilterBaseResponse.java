/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import java.util.List;

import com.clustercontrol.filtersetting.bean.EventFilterBaseInfo;
import com.clustercontrol.filtersetting.bean.FacilityTarget;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = EventFilterBaseInfo.class)
public class EventFilterBaseResponse {

	private String facilityId;
	private FacilityTarget facilityTarget;
	private Boolean entire;
	private List<EventFilterConditionResponse> conditions;

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

	public Boolean getEntire() {
		return entire;
	}

	public void setEntire(Boolean entire) {
		this.entire = entire;
	}

	public List<EventFilterConditionResponse> getConditions() {
		return conditions;
	}

	public void setConditions(List<EventFilterConditionResponse> conditions) {
		this.conditions = conditions;
	}

}
