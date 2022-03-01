/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ModifyPerformanceMonitorRequest extends AbstractModifyNumericMonitorRequest {

	public ModifyPerformanceMonitorRequest() {

	}

	private PerfCheckInfoRequest perfCheckInfo;

	
	public PerfCheckInfoRequest getPerfCheckInfo() {
		return perfCheckInfo;
	}


	public void setPerfCheckInfo(PerfCheckInfoRequest perfCheckInfo) {
		this.perfCheckInfo = perfCheckInfo;
	}

	@Override
	public String toString() {
		return "ModifyPerformanceMonitorRequest [perfCheckInfo=" + perfCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", itemName=" + itemName + ", measure=" + measure + ", predictionFlg=" + predictionFlg
				+ ", predictionMethod=" + predictionMethod + ", predictionAnalysysRange=" + predictionAnalysysRange
				+ ", predictionTarget=" + predictionTarget + ", predictionApplication=" + predictionApplication
				+ ", changeFlg=" + changeFlg + ", changeAnalysysRange=" + changeAnalysysRange + ", changeApplication="
				+ changeApplication + ", numericValueInfo=" + numericValueInfo + ", predictionNotifyRelationList="
				+ predictionNotifyRelationList + ", changeNotifyRelationList=" + changeNotifyRelationList
				+ ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId
				+ ", notifyRelationList=" + notifyRelationList + "]";
	}
}
