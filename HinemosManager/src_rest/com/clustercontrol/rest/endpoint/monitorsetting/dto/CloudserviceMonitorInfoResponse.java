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

public class CloudserviceMonitorInfoResponse extends AbstractTruthMonitorResponse {

	@RestBeanConvertEnum
	private PriorityChangeFailureTypeEnum priorityChangeFailureType;

	private PluginCheckInfoResponse pluginCheckInfo;

	public CloudserviceMonitorInfoResponse() {
	}

	public PluginCheckInfoResponse getPluginCheckInfo() {
		return pluginCheckInfo;
	}

	public void setPluginCheckInfo(PluginCheckInfoResponse pluginCheckInfo) {
		this.pluginCheckInfo = pluginCheckInfo;
	}

	public PriorityChangeFailureTypeEnum getPriorityChangeFailureType() {
		return priorityChangeFailureType;
	}

	public void setPriorityChangeFailureType(PriorityChangeFailureTypeEnum priorityChangeFailureType) {
		this.priorityChangeFailureType = priorityChangeFailureType;
	}

	@Override
	public String toString() {
		return "CloudserviceMonitorInfoResponse [pluginCheckInfo=" + pluginCheckInfo + ", truthValueInfo="
				+ truthValueInfo + ", monitorId=" + monitorId + ", application=" + application + ", description="
				+ description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", facilityId="
				+ facilityId + ", priorityChangeFailureType=" + priorityChangeFailureType + ", notifyRelationList="
				+ notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}
	
}