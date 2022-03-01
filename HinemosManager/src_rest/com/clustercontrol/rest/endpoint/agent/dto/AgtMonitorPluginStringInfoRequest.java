/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(to = MonitorPluginStringInfo.class)
@RestBeanConvertIdClassSet(infoClass = MonitorPluginStringInfo.class, idName = "id")
public class AgtMonitorPluginStringInfoRequest extends AgentRequestDto {
	private String key;
	private String value;
	public AgtMonitorPluginStringInfoRequest() {
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
