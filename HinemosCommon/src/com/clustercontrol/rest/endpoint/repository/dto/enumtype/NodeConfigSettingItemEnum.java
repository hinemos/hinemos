/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.rest.dto.EnumDto;

public enum NodeConfigSettingItemEnum implements EnumDto<String> {

	HOSTNAME(NodeConfigSettingItem.HOSTNAME.name()),
	OS(NodeConfigSettingItem.OS.name()),
	HW_CPU(NodeConfigSettingItem.HW_CPU.name()),
	HW_MEMORY(NodeConfigSettingItem.HW_MEMORY.name()),
	HW_NIC(NodeConfigSettingItem.HW_NIC.name()),
	HW_DISK(NodeConfigSettingItem.HW_DISK.name()),
	HW_FILESYSTEM(NodeConfigSettingItem.HW_FILESYSTEM.name()),
	NODE_VARIABLE(NodeConfigSettingItem.NODE_VARIABLE.name()),
	NETSTAT(NodeConfigSettingItem.NETSTAT.name()),
	PROCESS(NodeConfigSettingItem.PROCESS.name()),
	PACKAGE(NodeConfigSettingItem.PACKAGE.name()),
	PRODUCT(NodeConfigSettingItem.PRODUCT.name()),
	LICENSE(NodeConfigSettingItem.LICENSE.name()),
	CUSTOM(NodeConfigSettingItem.CUSTOM.name());

	private final String code;

	private NodeConfigSettingItemEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
