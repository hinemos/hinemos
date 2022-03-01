/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ModifyInfraFileRequest implements RequestDto {
	
	@RestItemName(value = MessageConstant.INFRA_FILEMANAGER_FILE_NAME)
	@RestValidateString(maxLen = 256, minLen = 1, notNull = true)
	private String fileName;

	public ModifyInfraFileRequest() {
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	@Override
	public String toString() {
		return "ModifyInfraFileRequest [fileName=" + fileName + "]";
	}
}
