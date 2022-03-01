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

public class GetRpaScenarioOperationResultSummaryStructureResponse {
	
	public GetRpaScenarioOperationResultSummaryStructureResponse() {
	}

	@RestPartiallyTransrateTarget
	private String item;
	@RestPartiallyTransrateTarget
	private String mesure;
	@RestPartiallyTransrateTarget
	private List<String> stacks;

	/** 項目名の単位 */
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}

	/** 単位 */
	public String getMesure() {
		return mesure;
	}
	public void setMesure(String mesure) {
		this.mesure = mesure;
	}

	/** GetRpaScenarioOperationResultSummaryDataResponseのvaluesの各区分 */
	public List<String> getStacks() {
		return stacks;
	}
	public void setStacks(List<String> stacks) {
		this.stacks = stacks;
	}

	@Override
	public String toString() {
		return "GetRpaScenarioOperationResultSummaryStructureResponse [item=" + item 
				+ ", mesure=" + mesure + ", stacks=" + stacks
				+ "]";
	}
}
