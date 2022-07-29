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
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AttachStorageRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_STORAGE_ID)
	@RestValidateString(notNull = true)
	private String storageId;

	@RestItemName(MessageConstant.XCLOUD_CORE_OPTIONS)
	@RestValidateCollection(notNull = true)
	private List<OptionRequest> option = new ArrayList<>();

	public AttachStorageRequest() {
	}

	public String getStorageId() {
		return storageId;
	}

	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}

	public List<OptionRequest> getOption() {
		return option;
	}

	public void setOption(List<OptionRequest> option) {
		this.option = option;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
