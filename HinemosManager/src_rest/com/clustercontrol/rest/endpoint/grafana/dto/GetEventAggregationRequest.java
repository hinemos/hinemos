/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.grafana.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.EventGroupByEnum;
import com.clustercontrol.util.MessageConstant;

public class GetEventAggregationRequest implements RequestDto {

	public GetEventAggregationRequest() {
	}

	@RestItemName(value = MessageConstant.EVENT_AGGREGATION_GROUP_BY)
	@RestValidateCollection(notNull = true, minSize = 1, maxSize = 2)
	private List<EventGroupByEnum> groupBy = new ArrayList<>();

	@RestItemName(value = MessageConstant.EVENT_AGGREGATION_SORT)
	@RestValidateCollection(notNull = true, minSize = 1, maxSize = 2)
	private List<GetEventAggregationSortRequest> sort = new ArrayList<>();

	@RestItemName(value = MessageConstant.EVENT_AGGREGATION_FILTER)
	@RestValidateObject(notNull = true)
	private GetEventAggregationFilterRequest filter;

	@RestItemName(value = MessageConstant.EVENT_AGGREGATION_SIZE)
	@RestValidateInteger(notNull = false, minVal = 0)
	private Integer size;

	public List<EventGroupByEnum> getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(List<EventGroupByEnum> groupBy) {
		this.groupBy = groupBy;
	}

	public List<GetEventAggregationSortRequest> getSort() {
		return sort;
	}

	public void setSort(List<GetEventAggregationSortRequest> sort) {
		this.sort = sort;
	}

	public GetEventAggregationFilterRequest getFilter() {
		return filter;
	}

	public void setFilter(GetEventAggregationFilterRequest filter) {
		this.filter = filter;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		List<String> groupByList = this.groupBy.stream().map(g -> g.toString()).collect(Collectors.toList());
		List<String> orderByList = this.sort.stream().map(o -> o.getSortKey().toString()).collect(Collectors.toList());
		Utils.validateGroupByAndOrderByCorrelation(groupByList, orderByList);

		filter.correlationCheck();
	}

}
