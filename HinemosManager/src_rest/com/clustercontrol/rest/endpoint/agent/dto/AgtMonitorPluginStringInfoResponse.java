/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertAssertion(from = MonitorPluginStringInfo.class)
@RestBeanConvertIdClassSet(infoClass = MonitorPluginStringInfo.class, idName = "id")
public class AgtMonitorPluginStringInfoResponse {
	private String key;
	private String value;

	public AgtMonitorPluginStringInfoResponse() {
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

}
