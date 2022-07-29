/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class MonitorInfoResponseP3 {

	private String monitorId;
	private String monitorTypeId;
	@RestPartiallyTransrateTarget
	private String measure;
	private String itemName;
	private Integer predictionTarget;
	private Integer predictionAnalysysRange;
	private List<MonitorNumericValueInfoResponse> numericValueInfo = new ArrayList<>();

	public MonitorInfoResponseP3() {
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorTypeId() {
		return monitorTypeId;
	}

	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}

	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public Integer getPredictionTarget() {
		return predictionTarget;
	}

	public void setPredictionTarget(Integer predictionTarget) {
		this.predictionTarget = predictionTarget;
	}

	public Integer getPredictionAnalysysRange() {
		return predictionAnalysysRange;
	}

	public void setPredictionAnalysysRange(Integer predictionAnalysysRange) {
		this.predictionAnalysysRange = predictionAnalysysRange;
	}

	public List<MonitorNumericValueInfoResponse> getNumericValueInfo() {
		return numericValueInfo;
	}

	public void setNumericValueInfo(List<MonitorNumericValueInfoResponse> numericValueInfo) {
		this.numericValueInfo = numericValueInfo;
	}

	@Override
	public String toString() {
		return "MonitorInfoResponseP3 [monitorId=" + monitorId + ", monitorTypeId=" + monitorTypeId + ", measure="
				+ measure + ", itemName=" + itemName + ", predictionTarget=" + predictionTarget
				+ ", predictionAnalysysRange=" + predictionAnalysysRange + ", numericValueInfo=" + numericValueInfo
				+ "]";
	}

}
