/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.infra.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;

public class InfraFileInfoResponse {
	private String fileId;
	private String fileName;
	private String createUserId;
	@RestBeanConvertDatetime
	private String createDatetime;
	private String modifyUserId;
	@RestBeanConvertDatetime
	private String modifyDatetime;
	private String ownerRoleId;

	public InfraFileInfoResponse() {
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

	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public String getCreateDatetime() {
		return createDatetime;
	}

	public void setCreateDatetime(String createDatetime) {
		this.createDatetime = createDatetime;
	}

	public String getModifyUserId() {
		return modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	public String getModifyDatetime() {
		return modifyDatetime;
	}

	public void setModifyDatetime(String modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	@Override
	public String toString() {
		return "InfraFileInfoResponse [fileId=" + fileId + ", fileName=" + fileName + ", createUserId=" + createUserId
				+ ", createDatetime=" + createDatetime + ", modifyUserId=" + modifyUserId + ", modifyDatetime="
				+ modifyDatetime + ", ownerRoleId=" + ownerRoleId + "]";
	}

}
