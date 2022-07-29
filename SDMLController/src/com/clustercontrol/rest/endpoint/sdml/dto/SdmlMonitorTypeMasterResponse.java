/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.sdml.model.SdmlMonitorTypeMasterInfo;

@RestBeanConvertIdClassSet(infoClass = SdmlMonitorTypeMasterInfo.class, idName = "id")
public class SdmlMonitorTypeMasterResponse {

	private String sdmlMonitorTypeId;

	@RestPartiallyTransrateTarget
	private String sdmlMonitorType;
	private String pluginId;

	public SdmlMonitorTypeMasterResponse() {
	}

	public String getSdmlMonitorTypeId() {
		return sdmlMonitorTypeId;
	}

	public void setSdmlMonitorTypeId(String sdmlMonitorTypeId) {
		this.sdmlMonitorTypeId = sdmlMonitorTypeId;
	}

	public String getSdmlMonitorType() {
		return sdmlMonitorType;
	}

	public void setSdmlMonitorType(String sdmlMonitorType) {
		this.sdmlMonitorType = sdmlMonitorType;
	}

	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}
}
