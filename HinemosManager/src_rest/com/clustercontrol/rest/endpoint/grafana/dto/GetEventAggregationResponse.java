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

public class GetEventAggregationResponse {

	public GetEventAggregationResponse() {
	}

	private String groupBy;
	private List<GetEventAggregationCountResponse> countLists = new ArrayList<>();

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	public List<GetEventAggregationCountResponse> getCountLists() {
		return countLists;
	}

	public void setCountLists(List<GetEventAggregationCountResponse> countLists) {
		this.countLists = countLists;
	}
}
