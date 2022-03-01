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
import com.clustercontrol.rest.endpoint.grafana.dto.enumtype.JobHistoryGroupByEnum;
import com.clustercontrol.util.MessageConstant;

public class GetJobHistoryAggregationRequest implements RequestDto {

	public GetJobHistoryAggregationRequest() {
	}

	@RestItemName(value = MessageConstant.JOB_HISTORY_AGGREGATION_GROUP_BY)
	@RestValidateCollection(notNull = true, minSize = 1, maxSize = 2)
	private List<JobHistoryGroupByEnum> groupBy = new ArrayList<>();

	@RestItemName(value = MessageConstant.JOB_HISTORY_AGGREGATION_SORT)
	@RestValidateCollection(notNull = true, minSize = 1, maxSize = 2)
	private List<GetJobHistoryAggregationSortRequest> sort = new ArrayList<>();

	@RestItemName(value = MessageConstant.JOB_HISTORY_AGGREGATION_FILTER)
	@RestValidateObject(notNull = true)
	private GetJobHistoryAggregationFilterRequest filter;

	@RestItemName(value = MessageConstant.JOB_HISTORY_AGGREGATION_SIZE)
	@RestValidateInteger(notNull = false, minVal = 0)
	private Integer size;

	public List<JobHistoryGroupByEnum> getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(List<JobHistoryGroupByEnum> groupBy) {
		this.groupBy = groupBy;
	}

	public List<GetJobHistoryAggregationSortRequest> getSort() {
		return sort;
	}

	public void setSort(List<GetJobHistoryAggregationSortRequest> sort) {
		this.sort = sort;
	}

	public GetJobHistoryAggregationFilterRequest getFilter() {
		return filter;
	}

	public void setFilter(GetJobHistoryAggregationFilterRequest filter) {
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
