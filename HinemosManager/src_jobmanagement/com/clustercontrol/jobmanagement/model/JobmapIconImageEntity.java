/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;



/**
 * The persistent class for the cc_jobmap_icon_image database table.
 * 
 */
@Entity
@Table(name="cc_jobmap_icon_image", schema="binarydata")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.JOBMAP_IMAGE_FILE,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="icon_id", insertable=false, updatable=false))
public class JobmapIconImageEntity extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	private String iconId;
	private byte[] filedata;
	private String description;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;

	@Deprecated
	public JobmapIconImageEntity() {
	}

	public JobmapIconImageEntity(String iconId) {
		this.setIconId(iconId);
		this.setObjectId(this.getIconId());
	}

	@Id
	@Column(name="icon_id")
	public String getIconId() {
		return this.iconId;
	}
	public void setIconId(String iconId) {
		this.iconId = iconId;
	}

	@Column(name="filedata")
	public byte[] getFiledata() {
		return this.filedata;
	}
	public void setFiledata(byte[] filedata) {
		this.filedata = filedata;
	}

	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
}