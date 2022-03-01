/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.dto.RequestDto;

public class SdmlControlSettingFilterInfoRequest implements RequestDto {

	private String applicationId;
	private String description;
	private String facilityId;
	private Boolean validFlg;
	private String regUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regFromDate;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regToDate;
	private String updateUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateFromDate;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateToDate;
	private String ownerRoleId;

	public SdmlControlSettingFilterInfoRequest() {
	}

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

	public String getRegFromDate() {
		return regFromDate;
	}

	public void setRegFromDate(String regFromDate) {
		this.regFromDate = regFromDate;
	}

	public String getRegToDate() {
		return regToDate;
	}

	public void setRegToDate(String regToDate) {
		this.regToDate = regToDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getUpdateFromDate() {
		return updateFromDate;
	}

	public void setUpdateFromDate(String updateFromDate) {
		this.updateFromDate = updateFromDate;
	}

	public String getUpdateToDate() {
		return updateToDate;
	}

	public void setUpdateToDate(String updateToDate) {
		this.updateToDate = updateToDate;
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
}
