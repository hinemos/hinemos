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
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.JobHistoryOrderEnum;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.JobHistorySortKeyEnum;
import com.clustercontrol.util.MessageConstant;

public class GetJobHistoryAggregationSortRequest implements RequestDto {

	public GetJobHistoryAggregationSortRequest() {
	}

	@RestItemName(value = MessageConstant.JOB_HISTORY_AGGREGATION_SORT_KEY)
	@RestValidateObject(notNull = true)
	private JobHistorySortKeyEnum sortKey;

	@RestItemName(value = MessageConstant.JOB_HISTORY_AGGREGATION_ORDER)
	private JobHistoryOrderEnum order = JobHistoryOrderEnum.ASC;

	public JobHistorySortKeyEnum getSortKey() {
		return sortKey;
	}

	public void setSortKey(JobHistorySortKeyEnum sortKey) {
		this.sortKey = sortKey;
	}

	public JobHistoryOrderEnum getOrder() {
		return order;
	}

	public void setOrder(JobHistoryOrderEnum order) {
		this.order = order;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
