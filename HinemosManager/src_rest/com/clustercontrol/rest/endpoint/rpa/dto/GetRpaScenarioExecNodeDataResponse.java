/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

public class GetRpaScenarioExecNodeDataResponse {
	
	public GetRpaScenarioExecNodeDataResponse() {
	}

	/** 実行ノード ファシリティID*/
	private String execNode;
	/** 実行ノード ノード名 */
	private String execNodeName;

	public String getExecNodeName() {
		return execNodeName;
	}
	public void setExecNodeName(String execNodeName) {
		this.execNodeName = execNodeName;
	}

	public String getExecNode() {
		return execNode;
	}
	public void setExecNode(String execNode) {
		this.execNode = execNode;
	}
	
	@Override
	public String toString() {
		return "RpaScenarioResponse ["
				+ "execNode=" + execNode + ", execNodeName=" + execNodeName + "]";
	}

}
