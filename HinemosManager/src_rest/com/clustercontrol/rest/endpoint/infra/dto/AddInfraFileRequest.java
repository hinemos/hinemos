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
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddInfraFileRequest implements RequestDto {
	
	@RestItemName(value = MessageConstant.INFRA_FILEMANAGER_FILE_ID)
	@RestValidateString(maxLen = 256, minLen = 1, notNull = true, type = CheckType.ID)
	private String fileId;
	
	@RestItemName(value = MessageConstant.INFRA_FILEMANAGER_FILE_NAME)
	@RestValidateString(maxLen = 256, minLen = 1, notNull = true)
	private String fileName;
	
	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(maxLen = 64, minLen = 1, notNull = true)
	private String ownerRoleId;

	public AddInfraFileRequest() {
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
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

	@Override
	public String toString() {
		return "AddInfraFileRequest [fileId=" + fileId + ", fileName=" + fileName + ", ownerRoleId=" + ownerRoleId
				+ "]";
	}

}
