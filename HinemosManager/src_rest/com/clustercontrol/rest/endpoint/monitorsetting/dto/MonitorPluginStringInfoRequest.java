/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.dto.RequestDto;

@RestBeanConvertIdClassSet(infoClass = MonitorPluginStringInfo.class, idName = "id")
public class MonitorPluginStringInfoRequest implements RequestDto {
	private String key;
	private String value;
	public MonitorPluginStringInfoRequest() {
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "MonitorPluginStringInfo [key=" + key + ", value=" + value + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
