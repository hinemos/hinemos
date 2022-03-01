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

public class ModifyInstanceRequest implements RequestDto {
	private String memo;

	@RestItemName(MessageConstant.XCLOUD_CORE_TAGS)
	@RestValidateCollection(notNull = true)
	private List<TagRequest> tags = new ArrayList<>();

	public ModifyInstanceRequest() {
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public List<TagRequest> getTags() {
		return tags;
	}

	public void setTags(List<TagRequest> tags) {
		this.tags = tags;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
