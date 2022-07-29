/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddUserInfoRequest implements RequestDto {

	public AddUserInfoRequest() {
	}

	@RestItemName(value = MessageConstant.USER_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String userId;

	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;

	@RestItemName(value = MessageConstant.USER_NAME)
	@RestValidateString(notNull = true, maxLen = 128)
	private String userName;

	@RestItemName(value = MessageConstant.MAIL_ADDRESS)
	@RestValidateString(maxLen = 1024)
	private String mailAddress;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMailAddress() {
		return mailAddress;
	}

	public void setMailAddress(String mailAddress) {
		this.mailAddress = mailAddress;
	}

	@Override
	public String toString() {
		return "AddUserInfoRequest [userId=" + userId + ", description=" + description + ", userName=" + userName
				+ ", mailAddress=" + mailAddress + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
