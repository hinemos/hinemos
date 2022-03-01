/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ModifyLogcountMonitorRequest extends AbstractModifyNumericMonitorRequest {

	public ModifyLogcountMonitorRequest() {

	}

	private LogcountCheckInfoRequest logcountCheckInfo;

	
	public LogcountCheckInfoRequest getLogcountCheckInfo() {
		return logcountCheckInfo;
	}


	public void setLogcountCheckInfo(LogcountCheckInfoRequest logcountCheckInfo) {
		this.logcountCheckInfo = logcountCheckInfo;
	}

	@Override
	public String toString() {
		return "ModifyLogcountMonitorRequest [logcountCheckInfo=" + logcountCheckInfo + ", collectorFlg=" + collectorFlg
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
