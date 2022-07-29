/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.bean;

import java.io.Serializable;

/**
 * SDML制御設定のフィルタ設定を格納するクラス
 *
 */
public class SdmlControlSettingFilterInfo implements Serializable {

	private static final long serialVersionUID = -9212922765032298911L;

	private String applicationId = null;
	private String description = null;
	private String facilityId = null;
	private Boolean validFlg = null;
	private String regUser = null;
	private Long regFromDate = 0l;
	private Long regToDate = 0l;
	private String updateUser = null;
	private Long updateFromDate = 0l;
	private Long updateToDate = 0l;
	private String ownerRoleId = null;

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public Long getRegFromDate() {
		return regFromDate;
	}

	public void setRegFromDate(Long regFromDate) {
		this.regFromDate = regFromDate;
	}

	public Long getRegToDate() {
		return regToDate;
	}

	public void setRegToDate(Long regToDate) {
		this.regToDate = regToDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getUpdateFromDate() {
		return updateFromDate;
	}

	public void setUpdateFromDate(Long updateFromDate) {
		this.updateFromDate = updateFromDate;
	}

	public Long getUpdateToDate() {
		return updateToDate;
	}

	public void setUpdateToDate(Long updateToDate) {
		this.updateToDate = updateToDate;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
}
