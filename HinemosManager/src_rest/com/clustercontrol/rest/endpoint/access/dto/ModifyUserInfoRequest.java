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
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ModifyUserInfoRequest implements RequestDto {

	public ModifyUserInfoRequest() {
	}

	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;

	@RestItemName(value = MessageConstant.PASSWORD)
	@RestValidateString(maxLen = 64)
	private String password;

	@RestItemName(value = MessageConstant.USER_NAME)
	@RestValidateString(notNull = true, maxLen = 128)
	private String userName;

	@RestItemName(value = MessageConstant.MAIL_ADDRESS)
	@RestValidateString(maxLen = 1024)
	private String mailAddress;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
		return "ModifyUserInfoRequest [description=" + description + ", password=" + password + ", userName=" + userName
				+ ", mailAddress=" + mailAddress + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
