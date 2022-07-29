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

public class GetStatusAggregationResponse {

	public GetStatusAggregationResponse() {
	}

	private String groupBy;
	private List<GetStatusAggregationCountResponse> countLists = new ArrayList<>();

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	public List<GetStatusAggregationCountResponse> getCountLists() {
		return countLists;
	}

	public void setCountLists(List<GetStatusAggregationCountResponse> countLists) {
		this.countLists = countLists;
	}
}
