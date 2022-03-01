/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(checksOpposite = false)
public class FilterSettingSummariesResponse {

	private List<FilterSettingSummaryResponse> summaries;

	public List<FilterSettingSummaryResponse> getSummaries() {
		return summaries;
	}

	public void setSummaries(List<FilterSettingSummaryResponse> summaries) {
		this.summaries = summaries;
	}

}
