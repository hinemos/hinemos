/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class ModifyJmxMonitorRequest extends AbstractModifyNumericMonitorRequest {

	public ModifyJmxMonitorRequest() {

	}

	private JmxCheckInfoRequest jmxCheckInfo;

	
	public JmxCheckInfoRequest getJmxCheckInfo() {
		return jmxCheckInfo;
	}


	public void setJmxCheckInfo(JmxCheckInfoRequest jmxCheckInfo) {
		this.jmxCheckInfo = jmxCheckInfo;
	}

	@Override
	public String toString() {
		return "ModifyJmxMonitorRequest [jmxCheckInfo=" + jmxCheckInfo + ", collectorFlg=" + collectorFlg
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
