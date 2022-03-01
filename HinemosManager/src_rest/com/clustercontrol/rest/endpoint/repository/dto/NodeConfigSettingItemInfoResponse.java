/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.NodeConfigSettingItemEnum;

@RestBeanConvertIdClassSet(infoClass = com.clustercontrol.repository.model.NodeConfigSettingItemInfo.class, idName = "id")
public class NodeConfigSettingItemInfoResponse {

	@RestBeanConvertEnum
	private NodeConfigSettingItemEnum settingItemId;

	public NodeConfigSettingItemInfoResponse() {
	}

	public NodeConfigSettingItemEnum getSettingItemId() {
		return settingItemId;
	}

	public void setSettingItemId(NodeConfigSettingItemEnum settingItemId) {
		this.settingItemId = settingItemId;
	}
}
