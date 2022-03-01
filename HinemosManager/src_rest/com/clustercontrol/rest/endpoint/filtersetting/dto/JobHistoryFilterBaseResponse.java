/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import java.util.List;

import com.clustercontrol.filtersetting.bean.JobHistoryFilterBaseInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = JobHistoryFilterBaseInfo.class)
public class JobHistoryFilterBaseResponse {

	private List<JobHistoryFilterConditionResponse> conditions;

	public List<JobHistoryFilterConditionResponse> getConditions() {
		return conditions;
	}

	public void setConditions(List<JobHistoryFilterConditionResponse> conditions) {
		this.conditions = conditions;
	}

}
