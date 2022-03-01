/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class CorrelationMonitorInfoResponse extends AbstractNumericMonitorResponse {

	private CorrelationCheckInfoResponse correlationCheckInfo;

	public CorrelationMonitorInfoResponse() {
	}
	
	public CorrelationCheckInfoResponse getCorrelationCheckInfo() {
		return correlationCheckInfo;
	}

	public void setCorrelationCheckInfo(CorrelationCheckInfoResponse correlationCheckInfo) {
		this.correlationCheckInfo = correlationCheckInfo;
	}

	@Override
	public String toString() {
		return "CorrelationMonitorInfoResponse [correlationCheckInfo=" + correlationCheckInfo + ", collectorFlg="
				+ collectorFlg + ", itemName=" + itemName + ", measure=" + measure + ", predictionFlg=" + predictionFlg
				+ ", predictionMethod=" + predictionMethod + ", predictionAnalysysRange=" + predictionAnalysysRange
				+ ", predictionTarget=" + predictionTarget + ", predictionApplication=" + predictionApplication
				+ ", changeFlg=" + changeFlg + ", changeAnalysysRange=" + changeAnalysysRange + ", changeApplication="
				+ changeApplication + ", numericValueInfo=" + numericValueInfo + ", predictionNotifyRelationList="
				+ predictionNotifyRelationList + ", changeNotifyRelationList=" + changeNotifyRelationList
				+ ", monitorId=" + monitorId + ", application=" + application + ", description=" + description
				+ ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", facilityId=" + facilityId
				+ ", notifyRelationList=" + notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

	
}