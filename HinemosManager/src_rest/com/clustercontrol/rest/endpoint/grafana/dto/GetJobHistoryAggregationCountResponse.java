/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.grafana.dto;

public class GetJobHistoryAggregationCountResponse {

	public GetJobHistoryAggregationCountResponse() {
	}

	private String value;
	private long count;
	private GetJobHistoryAggregationResponse nestGroupBy = new GetJobHistoryAggregationResponse();

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public GetJobHistoryAggregationResponse getNestGroupBy() {
		return nestGroupBy;
	}

	public void setNestGroupBy(GetJobHistoryAggregationResponse nestGroupBy) {
		this.nestGroupBy = nestGroupBy;
	}
}
