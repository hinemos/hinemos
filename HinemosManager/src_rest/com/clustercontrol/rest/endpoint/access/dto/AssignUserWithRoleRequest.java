/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AssignUserWithRoleRequest implements RequestDto {

	public AssignUserWithRoleRequest() {
	}

	@RestItemName(value = MessageConstant.USER_ID)
	private List<String> userIdList = new ArrayList<>();

	public List<String> getUserIdList() {
		return userIdList;
	}

	public void setUserIdList(List<String> userIdList) {
		this.userIdList = userIdList;
	}

	@Override
	public String toString() {
		return "AssignUserWithRoleRequest [userIdList=" + userIdList + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
