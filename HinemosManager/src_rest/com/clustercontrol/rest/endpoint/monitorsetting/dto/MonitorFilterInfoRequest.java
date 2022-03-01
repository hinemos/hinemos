/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.dto.RequestDto;

public class MonitorFilterInfoRequest implements RequestDto {

	public MonitorFilterInfoRequest() {

	}

	private String monitorId;
	private String monitorTypeId;
	private String description;
	private String facilityId;
	private String calendarId;
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
	private Boolean monitorFlg;
	private Boolean collectorFlg;
	private String ownerRoleId;


	public String getMonitorId() {
		return monitorId;
	}


	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}


	public String getMonitorTypeId() {
		return monitorTypeId;
	}


	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
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


	public String getCalendarId() {
		return calendarId;
	}


	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
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


	public Boolean getMonitorFlg() {
		return monitorFlg;
	}


	public void setMonitorFlg(Boolean monitorFlg) {
		this.monitorFlg = monitorFlg;
	}


	public Boolean getCollectorFlg() {
		return collectorFlg;
	}


	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}


	public String getOwnerRoleId() {
		return ownerRoleId;
	}


	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}


	@Override
	public String toString() {
		return "MonitorFilterInfoRequest [monitorId=" + monitorId + ", monitorTypeId=" + monitorTypeId
				+ ", description=" + description + ", facilityId=" + facilityId + ", calendarId=" + calendarId
				+ ", regUser=" + regUser + ", regFromDate=" + regFromDate + ", regToDate=" + regToDate + ", updateUser="
				+ updateUser + ", updateFromDate=" + updateFromDate + ", updateToDate=" + updateToDate + ", monitorFlg="
				+ monitorFlg + ", collectorFlg=" + collectorFlg + ", ownerRoleId=" + ownerRoleId + "]";
	}


	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
