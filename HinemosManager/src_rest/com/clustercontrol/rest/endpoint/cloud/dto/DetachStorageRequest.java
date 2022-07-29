/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class DetachStorageRequest implements RequestDto {

	@RestItemName(MessageConstant.XCLOUD_CORE_STORAGE_IDS)
	@RestValidateCollection(notNull = true)
	private List<String> storageIds = new ArrayList<>();

	public DetachStorageRequest() {
	}

	public List<String> getStorageIds() {
		return storageIds;
	}

	public void setStorageIds(List<String> storageIds) {
		this.storageIds = storageIds;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
