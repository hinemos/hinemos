/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddMailTemplateRequest implements RequestDto {

	public AddMailTemplateRequest() {
	}

	@RestItemName(value = MessageConstant.MAIL_TEMPLATE_ID)
	@RestValidateString(notNull = true, minLen = 1, type = CheckType.ID)
	private String mailTemplateId;
	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	private String ownerRoleId;
	private String body;
	@RestItemName(value = MessageConstant.DESCRIPTION)
	private String description;
	@RestItemName(value = MessageConstant.SUBJECT)
	private String subject;

	public String getMailTemplateId() {
		return this.mailTemplateId;
	}

	public void setMailTemplateId(String mailTemplateId) {
		this.mailTemplateId = mailTemplateId;
	}

	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getBody() {
		return this.body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public String toString() {
		return "AddMailTemplateRequest [mailTemplateId=" + mailTemplateId + ", body=" + body + ", description="
				+ description + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
