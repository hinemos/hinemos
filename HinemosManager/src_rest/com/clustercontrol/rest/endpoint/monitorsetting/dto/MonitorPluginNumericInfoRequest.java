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
import com.clustercontrol.monitor.plugin.model.MonitorPluginNumericInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.dto.RequestDto;

@RestBeanConvertIdClassSet(infoClass = MonitorPluginNumericInfo.class, idName = "id")
public class MonitorPluginNumericInfoRequest implements RequestDto {
	private String key;
	private Double value;
	public MonitorPluginNumericInfoRequest() {
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "MonitorPluginNumericInfo [key=" + key + ", value=" + value + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
