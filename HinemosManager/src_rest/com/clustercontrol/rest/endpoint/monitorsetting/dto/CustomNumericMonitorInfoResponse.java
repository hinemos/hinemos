/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeFailureTypeEnum;

public class CustomNumericMonitorInfoResponse extends AbstractNumericMonitorResponse {

	private CustomCheckInfoResponse customCheckInfo;

	@RestBeanConvertEnum
	private PriorityChangeFailureTypeEnum priorityChangeFailureType;

	public CustomNumericMonitorInfoResponse() {
	}

	public CustomCheckInfoResponse getCustomCheckInfo() {
		return customCheckInfo;
	}

	public void setCustomCheckInfo(CustomCheckInfoResponse customCheckInfo) {
		this.customCheckInfo = customCheckInfo;
	}

	public PriorityChangeFailureTypeEnum getPriorityChangeFailureType() {
		return priorityChangeFailureType;
	}

	public void setPriorityChangeFailureType(PriorityChangeFailureTypeEnum priorityChangeFailureType) {
		this.priorityChangeFailureType = priorityChangeFailureType;
	}

	@Override
	public String toString() {
		return "CustomNumericMonitorInfoResponse [customCheckInfo=" + customCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", itemName=" + itemName + ", measure=" + measure + ", predictionFlg=" + predictionFlg
				+ ", predictionMethod=" + predictionMethod + ", predictionAnalysysRange=" + predictionAnalysysRange
				+ ", predictionTarget=" + predictionTarget + ", predictionApplication=" + predictionApplication
				+ ", changeFlg=" + changeFlg + ", changeAnalysysRange=" + changeAnalysysRange + ", changeApplication="
				+ changeApplication + ", numericValueInfo=" + numericValueInfo + ", predictionNotifyRelationList="
				+ predictionNotifyRelationList + ", changeNotifyRelationList=" + changeNotifyRelationList
				+ ", monitorId=" + monitorId + ", application=" + application + ", description=" + description
				+ ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", facilityId=" + facilityId
				+ ", priorityChangeFailureType=" + priorityChangeFailureType + ", notifyRelationList="
				+ notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

}