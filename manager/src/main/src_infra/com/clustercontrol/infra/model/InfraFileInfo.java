/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;

@XmlType(namespace = "http://infra.ws.clustercontrol.com")
@Entity
@Table(name="cc_infra_file", schema="setting")
@HinemosObjectPrivilege(objectType=HinemosModuleConstant.INFRA_FILE, isModifyCheck=true)
@AttributeOverride(name="objectId", column=@Column(name="file_id", insertable=false, updatable=false))
@Cacheable(true)
public class InfraFileInfo extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	
	private String fileId;
	private String fileName;
	private String createUserId;
	private Long createDatetime;
	private String modifyUserId;
	private Long modifyDatetime;
	private InfraFileContentEntity infraFileContentEntity;
	
	public InfraFileInfo() {
	}
	
	public InfraFileInfo(String fileId, String fileName) {
		setFileId(fileId);
		this.fileName = fileName;
	}

	@Id
	@Column(name="file_id")
	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
		setObjectId(fileId);
	}

	@Column(name="file_name")
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name="create_user_id")
	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	@Column(name="create_datetime")
	public Long getCreateDatetime() {
		return createDatetime;
	}

	public void setCreateDatetime(Long createDatetime) {
		this.createDatetime = createDatetime;
	}

	@Column(name="modify_user_id")
	public String getModifyUserId() {
		return modifyUserId;
	}

	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
	}

	@Column(name="modify_datetime")
	public Long getModifyDatetime() {
		return modifyDatetime;
	}

	public void setModifyDatetime(Long modifyDatetime) {
		this.modifyDatetime = modifyDatetime;
	}
	
	@XmlTransient
	@OneToOne(fetch=FetchType.LAZY)
	@PrimaryKeyJoinColumn(name="file_id")
	public InfraFileContentEntity getInfraFileContentEntity() {
		return this.infraFileContentEntity;
	}
	public void setInfraFileContentEntity(InfraFileContentEntity infraFileContentEntity) {
		this.infraFileContentEntity = infraFileContentEntity;
	}
}