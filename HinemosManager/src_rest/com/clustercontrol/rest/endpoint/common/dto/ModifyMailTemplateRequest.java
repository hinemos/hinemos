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
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ModifyMailTemplateRequest implements RequestDto {

	public ModifyMailTemplateRequest() {
	}

	private String body;
	@RestItemName(value = MessageConstant.DESCRIPTION)
	private String description;
	@RestItemName(value = MessageConstant.SUBJECT)
	private String subject;

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
		return "ModifyMailTemplateRequest [body=" + body + ", description=" + description + ", subject=" + subject
				+ "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
