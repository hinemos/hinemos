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

public class GetRpaScenarioOperationResultSummaryForPieResponse {

	public GetRpaScenarioOperationResultSummaryForPieResponse() {
	}

	@RestPartiallyTransrateTarget
	private String name;
	private GetRpaScenarioOperationResultSummaryStructureResponse structure;
	private List<Double> datas;

	/** グラフ自体の名前 */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	/** 単位を保持するクラス */
	public GetRpaScenarioOperationResultSummaryStructureResponse getStructure() {
		return structure;
	}
	public void setStructure(GetRpaScenarioOperationResultSummaryStructureResponse structure) {
		this.structure = structure;
	}

	/** グラフに表示するデータ */
	public List<Double> getDatas() {
		return datas;
	}
	public void setDatas(List<Double> datas) {
		this.datas = datas;
	}

	@Override
	public String toString() {
		return "GetRpaScenarioOperationResultSummaryForBarResponse [name=" + name 
				+ ", structure=" + structure + ", datas=" + datas
				+ "]";
	}

}
