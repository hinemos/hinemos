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
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.StatusOrderEnum;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.StatusSortKeyEnum;
import com.clustercontrol.util.MessageConstant;

public class GetStatusAggregationSortRequest implements RequestDto {

	public GetStatusAggregationSortRequest() {
	}

	@RestItemName(value = MessageConstant.EVENT_AGGREGATION_SORT_KEY)
	@RestValidateObject(notNull = true)
	private StatusSortKeyEnum sortKey;

	@RestItemName(value = MessageConstant.EVENT_AGGREGATION_ORDER)
	private StatusOrderEnum order = StatusOrderEnum.ASC;

	public StatusSortKeyEnum getSortKey() {
		return sortKey;
	}

	public void setSortKey(StatusSortKeyEnum sortKey) {
		this.sortKey = sortKey;
	}

	public StatusOrderEnum getOrder() {
		return order;
	}

	public void setOrder(StatusOrderEnum order) {
		this.order = order;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
