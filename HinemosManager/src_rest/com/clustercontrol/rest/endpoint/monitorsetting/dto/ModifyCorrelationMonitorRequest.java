/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ModifyCorrelationMonitorRequest extends AbstractModifyNumericMonitorRequest {

	public ModifyCorrelationMonitorRequest() {

	}

	private CorrelationCheckInfoRequest correlationCheckInfo;

	
	public CorrelationCheckInfoRequest getCorrelationCheckInfo() {
		return correlationCheckInfo;
	}


	public void setCorrelationCheckInfo(CorrelationCheckInfoRequest correlationCheckInfo) {
		this.correlationCheckInfo = correlationCheckInfo;
	}


	@Override
	public String toString() {
		return "ModifyCorrelationMonitorRequest [correlationCheckInfo=" + correlationCheckInfo + ", collectorFlg="
				+ collectorFlg + ", itemName=" + itemName + ", measure=" + measure + ", predictionFlg=" + predictionFlg
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
