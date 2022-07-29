/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;


import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.util.MessageConstant;

public class AddRestAccessInfoRequest extends ModifyRestAccessInfoRequest {
	
	@RestItemName(value = MessageConstant.REST_ACCESS_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String restAccessId;

	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String ownerRoleId;

	public AddRestAccessInfoRequest() {
	}

	public String getRestAccessId() {
		return restAccessId;
	}

	public void setRestAccessId(String restAccessId) {
		this.restAccessId = restAccessId;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

}
