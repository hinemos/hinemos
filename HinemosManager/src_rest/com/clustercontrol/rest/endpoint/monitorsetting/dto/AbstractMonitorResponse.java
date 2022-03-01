/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.MonitorTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.RunIntervalEnum;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;

public abstract class AbstractMonitorResponse {

	public AbstractMonitorResponse() {

	}

	protected String monitorId;
	@RestBeanConvertEnum
	protected MonitorTypeEnum monitorType;
	protected String monitorTypeId;
	protected String application;
	protected String description;
	protected Boolean monitorFlg;
	@RestBeanConvertEnum
	protected RunIntervalEnum runInterval;
	protected String calendarId;
	protected String facilityId;
	@RestPartiallyTransrateTarget
	protected String scope;
	protected List<NotifyRelationInfoResponse> notifyRelationList = new ArrayList<>();
	protected String ownerRoleId;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	protected String regDate;
	protected String regUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	protected String updateDate;
	protected String updateUser;
	protected String sdmlMonitorTypeId;

	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	public MonitorTypeEnum getMonitorType() {
		return monitorType;
	}
	public void setMonitorType(MonitorTypeEnum monitorType) {
		this.monitorType = monitorType;
	}
	public String getMonitorTypeId() {
		return monitorTypeId;
	}
	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getMonitorFlg() {
		return monitorFlg;
	}
	public void setMonitorFlg(Boolean monitorFlg) {
		this.monitorFlg = monitorFlg;
	}
	public RunIntervalEnum getRunInterval() {
		return runInterval;
	}
	public void setRunInterval(RunIntervalEnum runInterval) {
		this.runInterval = runInterval;
	}
	public String getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public List<NotifyRelationInfoResponse> getNotifyRelationList() {
		return notifyRelationList;
	}
	public void setNotifyRelationList(List<NotifyRelationInfoResponse> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	public String getRegDate() {
		return regDate;
	}
	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}
	public String getRegUser() {
		return regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	public String getSdmlMonitorTypeId() {
		return sdmlMonitorTypeId;
	}
	public void setSdmlMonitorTypeId(String sdmlMonitorTypeId) {
		this.sdmlMonitorTypeId = sdmlMonitorTypeId;
	}

	
}
