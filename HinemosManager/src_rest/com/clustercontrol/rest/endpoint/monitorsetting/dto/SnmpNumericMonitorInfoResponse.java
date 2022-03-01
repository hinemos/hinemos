/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class SnmpNumericMonitorInfoResponse extends AbstractNumericMonitorResponse {

	private SnmpCheckInfoResponse snmpCheckInfo;

	public SnmpNumericMonitorInfoResponse() {
	}

	public SnmpCheckInfoResponse getSnmpCheckInfo() {
		return snmpCheckInfo;
	}

	public void setSnmpCheckInfo(SnmpCheckInfoResponse snmpCheckInfo) {
		this.snmpCheckInfo = snmpCheckInfo;
	}

	@Override
	public String toString() {
		return "SnmpNumericMonitorInfoResponse [snmpCheckInfo=" + snmpCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", itemName=" + itemName + ", measure=" + measure + ", predictionFlg=" + predictionFlg
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