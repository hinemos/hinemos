/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.NodeConfigSettingItemEnum;

@RestBeanConvertIdClassSet(infoClass = com.clustercontrol.repository.model.NodeConfigSettingItemInfo.class, idName = "id")
public class NodeConfigSettingItemInfoRequest implements RequestDto {

	@RestBeanConvertEnum
	private NodeConfigSettingItemEnum settingItemId;

	public NodeConfigSettingItemInfoRequest() {
	}

	public NodeConfigSettingItemEnum getSettingItemId() {
		return settingItemId;
	}

	public void setSettingItemId(NodeConfigSettingItemEnum settingItemId) {
		this.settingItemId = settingItemId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
