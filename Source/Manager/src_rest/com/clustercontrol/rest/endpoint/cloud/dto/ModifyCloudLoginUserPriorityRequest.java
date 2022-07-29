/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ModifyCloudLoginUserPriorityRequest implements RequestDto {

	@RestItemName(MessageConstant.XCLOUD_CORE_CLOUDLOGINUSER_IDS)
	@RestValidateObject(notNull = true)
	private List<String> cloudLoginUserIdList;

	public ModifyCloudLoginUserPriorityRequest() {
	}

	public List<String> getCloudLoginUserIdList() {
		return cloudLoginUserIdList;
	}

	public void setCloudLoginUserIdList(List<String> cloudLoginUserIdList) {
		this.cloudLoginUserIdList = cloudLoginUserIdList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
