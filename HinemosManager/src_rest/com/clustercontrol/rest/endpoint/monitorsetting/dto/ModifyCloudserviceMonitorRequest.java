/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeFailureTypeEnum;
import com.clustercontrol.util.MessageConstant;

public class ModifyCloudserviceMonitorRequest extends AbstractModifyTruthMonitorRequest {

	@RestItemName(value = MessageConstant.PRIORITY_CHANGE_FAILURE_TYPE)
	@RestValidateObject(notNull = true)
	@RestBeanConvertEnum
	protected PriorityChangeFailureTypeEnum priorityChangeFailureType;

	public ModifyCloudserviceMonitorRequest() {

	}

	private PluginCheckInfoRequest pluginCheckInfo;

	
	public PluginCheckInfoRequest getPluginCheckInfo() {
		return pluginCheckInfo;
	}


	public void setPluginCheckInfo(PluginCheckInfoRequest pluginCheckInfo) {
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
		return "ModifyCloudserviceMonitorRequest [pluginCheckInfo=" + pluginCheckInfo + ", truthValueInfo="
				+ truthValueInfo + ", application=" + application + ", description=" + description + ", monitorFlg="
				+ monitorFlg + ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId="
				+ facilityId + ", priorityChangeFailureType=" + priorityChangeFailureType + ", notifyRelationList="
				+ notifyRelationList + "]";
	}
}
