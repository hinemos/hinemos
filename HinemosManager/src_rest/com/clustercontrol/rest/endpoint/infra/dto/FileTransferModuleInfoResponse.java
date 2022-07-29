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

import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.SendMethodTypeEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;

@RestBeanConvertIdClassSet(infoClass = InfraModuleInfo.class, idName = "id")
public class FileTransferModuleInfoResponse extends InfraModuleInfoResponse{
	
	private String fileId;
	
	private String destPath;
	
	@RestBeanConvertEnum
	private SendMethodTypeEnum sendMethodType;
	
	private String destOwner;
	
	private String destAttribute;
	
	private Boolean backupIfExistFlg;
	
	private List<FileTransferVariableInfoResponse> fileTransferVariableInfoEntities = new ArrayList<>();

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

	public List<FileTransferVariableInfoResponse> getFileTransferVariableInfoEntities() {
		return fileTransferVariableInfoEntities;
	}

	public void setFileTransferVariableInfoEntities(List<FileTransferVariableInfoResponse> fileTransferVariableInfoEntities) {
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
		return "FileTransferModuleInfoResponse [destPath=" + destPath + ", sendMethodType=" + sendMethodType
				+ ", destOwner=" + destOwner + ", destAttribute=" + destAttribute + ", backupIfExistFlg="
				+ backupIfExistFlg + ", fileTransferVariableInfoEntities=" + fileTransferVariableInfoEntities
				+ ", fileId=" + fileId + "]";
	}
}
