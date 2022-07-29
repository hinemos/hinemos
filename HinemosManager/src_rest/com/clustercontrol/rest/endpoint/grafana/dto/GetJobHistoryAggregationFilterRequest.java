/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.grafana.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class GetJobHistoryAggregationFilterRequest implements RequestDto {

	public GetJobHistoryAggregationFilterRequest() {
	}

	@RestItemName(value = MessageConstant.JOB_HISTORY_AGGREGATION_CONDITIONS)
	private GetJobHistoryAggregationConditionsRequest condition = new GetJobHistoryAggregationConditionsRequest();

	public GetJobHistoryAggregationConditionsRequest getCondition() {
		return condition;
	}

	public void setCondition(GetJobHistoryAggregationConditionsRequest condition) {
		this.condition = condition;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		if (condition != null) {
			condition.correlationCheck();
		}
	}

}
