/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddJobLinkSendSettingRequest extends AbstractJobLinkSendSettingRequest implements RequestDto {

	@RestItemName(value=MessageConstant.JOBLINK_SEND_SETTING_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String joblinkSendSettingId;

	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(notNull = true)
	private String ownerRoleId;

	public AddJobLinkSendSettingRequest() {
	}
	
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public String getJoblinkSendSettingId() {
		return joblinkSendSettingId;
	}

	public void setJoblinkSendSettingId(String joblinkSendSettingId) {
		this.joblinkSendSettingId = joblinkSendSettingId;
	}
}
