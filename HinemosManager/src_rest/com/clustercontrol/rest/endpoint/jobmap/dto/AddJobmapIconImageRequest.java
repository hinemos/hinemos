/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.jobmap.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddJobmapIconImageRequest implements RequestDto {

	public AddJobmapIconImageRequest() {
	}

	@RestItemName(value = MessageConstant.ICON_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID )
	private String iconId;

	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64 )
	private String description;

	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64 )
	private String ownerRoleId;

	public String getIconId() {
		return iconId;
	}

	public void setIconId(String iconId) {
		this.iconId = iconId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
