/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.SendMethodTypeEnum;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertIdClassSet(infoClass = InfraModuleInfo.class, idName = "id")
public class FileTransferModuleInfoRequest extends InfraModuleInfoRequest {

	@RestItemName(MessageConstant.INFRA_FILEMANAGER_FILE_ID)
	@RestValidateString(maxLen = 256, minLen = 1)
	private String fileId;
	
	@RestItemName(MessageConstant.INFRA_MODULE_PLACEMENT_PATH)
	@RestValidateString(maxLen = 1024, minLen = 1)
	private String destPath;
	
	@RestBeanConvertEnum
	private SendMethodTypeEnum sendMethodType;
	
	@RestItemName(MessageConstant.INFRA_MODULE_TRANSFER_METHOD_OWNER)
	@RestValidateString(maxLen = 256, minLen = 1)
	private String destOwner;
	
	@RestItemName(MessageConstant.INFRA_MODULE_TRANSFER_METHOD_SCP_FILE_ATTRIBUTE)
	@RestValidateString(maxLen = 64, minLen = 1)
	private String destAttribute;
	
	// ファイル配置時に古いファイルをリネームして保持する
	private Boolean backupIfExistFlg;

	private List<FileTransferVariableInfoRequest> fileTransferVariableInfoEntities = new ArrayList<>();

	public String getDestPath() {
		return destPath;
	}

	public void setDestPath(String destPath) {
		this.destPath = destPath;
	}

	public SendMethodTypeEnum getSendMethodType() {
		return sendMethodType;
	}

	public void setSendMethodType(SendMethodTypeEnum sendMethodType) {
		this.sendMethodType = sendMethodType;
	}

	public String getDestOwner() {
		return destOwner;
	}

	public void setDestOwner(String destOwner) {
		this.destOwner = destOwner;
	}

	public String getDestAttribute() {
		return destAttribute;
	}

	public void setDestAttribute(String destAttribute) {
		this.destAttribute = destAttribute;
	}

	public Boolean getBackupIfExistFlg() {
		return backupIfExistFlg;
	}

	public void setBackupIfExistFlg(Boolean backupIfExistFlg) {
		this.backupIfExistFlg = backupIfExistFlg;
	}

	public List<FileTransferVariableInfoRequest> getFileTransferVariableInfoEntities() {
		return fileTransferVariableInfoEntities;
	}

	public void setFileTransferVariableInfoEntities(
			List<FileTransferVariableInfoRequest> fileTransferVariableInfoEntities) {
		this.fileTransferVariableInfoEntities = fileTransferVariableInfoEntities;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	@Override
	public String toString() {
		return "FileTransferModuleInfoRequest [destPath=" + destPath + ", sendMethodType=" + sendMethodType
				+ ", destOwner=" + destOwner + ", destAttribute=" + destAttribute + ", backupIfExistFlg="
				+ backupIfExistFlg + ", fileTransferVariableInfoEntities=" + fileTransferVariableInfoEntities
				+ ", fileId=" + fileId + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
