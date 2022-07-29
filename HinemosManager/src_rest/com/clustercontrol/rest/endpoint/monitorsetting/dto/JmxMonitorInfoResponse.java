/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class JmxMonitorInfoResponse extends AbstractNumericMonitorResponse {

	private JmxCheckInfoResponse jmxCheckInfo;

	public JmxMonitorInfoResponse() {
	}

	public JmxCheckInfoResponse getJmxCheckInfo() {
		return jmxCheckInfo;
	}

	public void setJmxCheckInfo(JmxCheckInfoResponse jmxCheckInfo) {
		this.jmxCheckInfo = jmxCheckInfo;
	}

	@Override
	public String toString() {
		return "JmxMonitorInfoResponse [jmxCheckInfo=" + jmxCheckInfo + ", collectorFlg=" + collectorFlg + ", itemName="
				+ itemName + ", measure=" + measure + ", predictionFlg=" + predictionFlg + ", predictionMethod="
				+ predictionMethod + ", predictionAnalysysRange=" + predictionAnalysysRange + ", predictionTarget="
				+ predictionTarget + ", predictionApplication=" + predictionApplication + ", changeFlg=" + changeFlg
				+ ", changeAnalysysRange=" + changeAnalysysRange + ", changeApplication=" + changeApplication
				+ ", numericValueInfo=" + numericValueInfo + ", predictionNotifyRelationList="
				+ predictionNotifyRelationList + ", changeNotifyRelationList=" + changeNotifyRelationList
				+ ", monitorId=" + monitorId + ", application=" + application + ", description=" + description
				+ ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", facilityId=" + facilityId
				+ ", notifyRelationList=" + notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

	
}