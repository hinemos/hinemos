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
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.EventOrderEnum;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.EventSortKeyEnum;
import com.clustercontrol.util.MessageConstant;

public class GetEventAggregationSortRequest implements RequestDto {

	public GetEventAggregationSortRequest() {
	}

	@RestItemName(value = MessageConstant.EVENT_AGGREGATION_SORT_KEY)
	@RestValidateObject(notNull = true)
	private EventSortKeyEnum sortKey;

	@RestItemName(value = MessageConstant.EVENT_AGGREGATION_ORDER)
	private EventOrderEnum order = EventOrderEnum.ASC;

	public EventSortKeyEnum getSortKey() {
		return sortKey;
	}

	public void setSortKey(EventSortKeyEnum sortKey) {
		this.sortKey = sortKey;
	}

	public EventOrderEnum getOrder() {
		return order;
	}

	public void setOrder(EventOrderEnum order) {
		this.order = order;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
