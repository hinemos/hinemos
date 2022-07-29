/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ModifySnmpNumericMonitorRequest extends AbstractModifyNumericMonitorRequest {

	public ModifySnmpNumericMonitorRequest() {

	}

	private SnmpCheckInfoRequest snmpCheckInfo;

	
	public SnmpCheckInfoRequest getSnmpCheckInfo() {
		return snmpCheckInfo;
	}


	public void setSnmpCheckInfo(SnmpCheckInfoRequest snmpCheckInfo) {
		this.snmpCheckInfo = snmpCheckInfo;
	}

	@Override
	public String toString() {
		return "ModifySnmpNumericMonitorRequest [snmpCheckInfo=" + snmpCheckInfo + ", collectorFlg=" + collectorFlg
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
