/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;
import com.clustercontrol.rest.endpoint.sdml.dto.enumtype.PriorityEnum;

public class SdmlControlSettingInfoResponse {

	private String ownerRoleId;

	private String applicationId;
	private String description;
	private String facilityId;
	private String controlLogDirectory;
	private String controlLogFilename;
	private Boolean controlLogCollectFlg;
	private String application;
	private Boolean validFlg;
	private Boolean autoMonitorDeleteFlg;
	private String autoMonitorCalendarId;
	private Integer earlyStopThresholdSecond;
	@RestBeanConvertEnum
	private PriorityEnum earlyStopNotifyPriority;
	@RestBeanConvertEnum
	private PriorityEnum autoCreateSuccessPriority;
	@RestBeanConvertEnum
	private PriorityEnum autoEnableSuccessPriority;
	@RestBeanConvertEnum
	private PriorityEnum autoDisableSuccessPriority;
	@RestBeanConvertEnum
	private PriorityEnum autoUpdateSuccessPriority;
	@RestBeanConvertEnum
	private PriorityEnum autoControlFailedPriority;
	private String regUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	private String updateUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	private String version;

	private List<SdmlMonitorNotifyRelationResponse> sdmlMonitorNotifyRelationList = new ArrayList<>();
	private List<NotifyRelationInfoResponse> notifyRelationList = new ArrayList<>();
	private List<NotifyRelationInfoResponse> autoMonitorCommonNotifyRelationList = new ArrayList<>();

	@RestPartiallyTransrateTarget
	private String scope;

	public SdmlControlSettingInfoResponse() {
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
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

	public String getControlLogDirectory() {
		return controlLogDirectory;
	}

	public void setControlLogDirectory(String controlLogDirectory) {
		this.controlLogDirectory = controlLogDirectory;
	}

	public String getControlLogFilename() {
		return controlLogFilename;
	}

	public void setControlLogFilename(String controlLogFilename) {
		this.controlLogFilename = controlLogFilename;
	}

	public Boolean getControlLogCollectFlg() {
		return controlLogCollectFlg;
	}

	public void setControlLogCollectFlg(Boolean controlLogCollectFlg) {
		this.controlLogCollectFlg = controlLogCollectFlg;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	public Boolean getAutoMonitorDeleteFlg() {
		return autoMonitorDeleteFlg;
	}

	public void setAutoMonitorDeleteFlg(Boolean autoMonitorDeleteFlg) {
		this.autoMonitorDeleteFlg = autoMonitorDeleteFlg;
	}

	public String getAutoMonitorCalendarId() {
		return autoMonitorCalendarId;
	}

	public void setAutoMonitorCalendarId(String autoMonitorCalendarId) {
		this.autoMonitorCalendarId = autoMonitorCalendarId;
	}

	public Integer getEarlyStopThresholdSecond() {
		return earlyStopThresholdSecond;
	}

	public void setEarlyStopThresholdSecond(Integer earlyStopThresholdSecond) {
		this.earlyStopThresholdSecond = earlyStopThresholdSecond;
	}

	public PriorityEnum getEarlyStopNotifyPriority() {
		return earlyStopNotifyPriority;
	}

	public void setEarlyStopNotifyPriority(PriorityEnum earlyStopNotifyPriority) {
		this.earlyStopNotifyPriority = earlyStopNotifyPriority;
	}

	public PriorityEnum getAutoCreateSuccessPriority() {
		return autoCreateSuccessPriority;
	}

	public void setAutoCreateSuccessPriority(PriorityEnum autoCreateSuccessPriority) {
		this.autoCreateSuccessPriority = autoCreateSuccessPriority;
	}

	public PriorityEnum getAutoEnableSuccessPriority() {
		return autoEnableSuccessPriority;
	}

	public void setAutoEnableSuccessPriority(PriorityEnum autoEnableSuccessPriority) {
		this.autoEnableSuccessPriority = autoEnableSuccessPriority;
	}

	public PriorityEnum getAutoDisableSuccessPriority() {
		return autoDisableSuccessPriority;
	}

	public void setAutoDisableSuccessPriority(PriorityEnum autoDisableSuccessPriority) {
		this.autoDisableSuccessPriority = autoDisableSuccessPriority;
	}

	public PriorityEnum getAutoUpdateSuccessPriority() {
		return autoUpdateSuccessPriority;
	}

	public void setAutoUpdateSuccessPriority(PriorityEnum autoUpdateSuccessPriority) {
		this.autoUpdateSuccessPriority = autoUpdateSuccessPriority;
	}

	public PriorityEnum getAutoControlFailedPriority() {
		return autoControlFailedPriority;
	}

	public void setAutoControlFailedPriority(PriorityEnum autoControlFailedPriority) {
		this.autoControlFailedPriority = autoControlFailedPriority;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<SdmlMonitorNotifyRelationResponse> getSdmlMonitorNotifyRelationList() {
		return sdmlMonitorNotifyRelationList;
	}

	public void setSdmlMonitorNotifyRelationList(
			List<SdmlMonitorNotifyRelationResponse> sdmlMonitorNotifyRelationList) {
		this.sdmlMonitorNotifyRelationList = sdmlMonitorNotifyRelationList;
	}

	public List<NotifyRelationInfoResponse> getNotifyRelationList() {
		return notifyRelationList;
	}

	public void setNotifyRelationList(List<NotifyRelationInfoResponse> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}

	public List<NotifyRelationInfoResponse> getAutoMonitorCommonNotifyRelationList() {
		return autoMonitorCommonNotifyRelationList;
	}

	public void setAutoMonitorCommonNotifyRelationList(
			List<NotifyRelationInfoResponse> autoMonitorCommonNotifyRelationList) {
		this.autoMonitorCommonNotifyRelationList = autoMonitorCommonNotifyRelationList;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}
