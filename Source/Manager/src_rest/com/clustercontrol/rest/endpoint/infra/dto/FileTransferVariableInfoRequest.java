/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.infra.model.FileTransferVariableInfo;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertIdClassSet(infoClass = FileTransferVariableInfo.class, idName = "id")
public class FileTransferVariableInfoRequest implements RequestDto {

	@RestItemName(MessageConstant.INFRA_MANAGEMENT_SEARCH_WORDS)
	@RestValidateString(maxLen = 256, minLen = 1)
	private String name;

	@RestItemName(MessageConstant.INFRA_MANAGEMENT_REPLACEMENT_WORDS)
	@RestValidateString(maxLen = 256, minLen = 1)
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	@Override
	public String toString() {
		return "FileTransferVariableInfoRequest [name=" + name + ", value=" + value + "]";
	}
}
