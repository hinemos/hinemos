/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class GetRpaScenarioOperationResultSummaryDataResponse {
	
	public GetRpaScenarioOperationResultSummaryDataResponse() {
	}

	@RestPartiallyTransrateTarget
	private String item;
	private List<Double> values;

	/** 項目名 */
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	
	/** グラフを形成する値（[成功の値,失敗の値]などで構成される） */
	public List<Double> getValues() {
		return values;
	}
	public void setValues(List<Double> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return "SearchRpaScenarioOperationResultResponse [item=" + item 
				+ ", values=" + values
				+ "]";
	}
}
