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

public class GetEventAggregationCountResponse {

	public GetEventAggregationCountResponse() {
	}

	private String value;
	private long count;
	private GetEventAggregationResponse nestGroupBy = new GetEventAggregationResponse();

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

	public GetEventAggregationResponse getNestGroupBy() {
		return nestGroupBy;
	}

	public void setNestGroupBy(GetEventAggregationResponse nestGroupBy) {
		this.nestGroupBy = nestGroupBy;
	}
}
