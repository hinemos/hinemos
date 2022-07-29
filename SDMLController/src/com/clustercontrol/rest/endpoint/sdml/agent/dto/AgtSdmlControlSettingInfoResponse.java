/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.agent.dto;

public class AgtSdmlControlSettingInfoResponse {

	// ---- from ObjectPrivilegeTargetInfo
	private String ownerRoleId;

	// ---- from SdmlControlSettingInfo
	private String applicationId;
	private String description;
	private String facilityId;
	private String controlLogDirectory;
	private String controlLogFilename;
	private Boolean controlLogCollectFlg;
	private String notifyGroupId;
	private String application;
	private Boolean validFlg;
	private Boolean autoMonitorDeleteFlg;
	private String autoMonitorCalendarId;
	private String autoMonitorCommonNotifyGroupId;
	private Integer earlyStopThresholdSecond;
	private Integer earlyStopNotifyPriority;
	private Integer autoCreateSuccessPriority;
	private Integer autoEnableSuccessPriority;
	private Integer autoDisableSuccessPriority;
	private Integer autoUpdateSuccessPriority;
	private Integer autoControlFailedPriority;
	private String regUser;
	private Long regDate;
	private String updateUser;
	private Long updateDate;
	private String version;

	public AgtSdmlControlSettingInfoResponse() {
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

	public String getNotifyGroupId() {
		return notifyGroupId;
	}

	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
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

	public String getAutoMonitorCommonNotifyGroupId() {
		return autoMonitorCommonNotifyGroupId;
	}

	public void setAutoMonitorCommonNotifyGroupId(String autoMonitorCommonNotifyGroupId) {
		this.autoMonitorCommonNotifyGroupId = autoMonitorCommonNotifyGroupId;
	}

	public Integer getEarlyStopThresholdSecond() {
		return earlyStopThresholdSecond;
	}

	public void setEarlyStopThresholdSecond(Integer earlyStopThresholdSecond) {
		this.earlyStopThresholdSecond = earlyStopThresholdSecond;
	}

	public Integer getEarlyStopNotifyPriority() {
		return earlyStopNotifyPriority;
	}

	public void setEarlyStopNotifyPriority(Integer earlyStopNotifyPriority) {
		this.earlyStopNotifyPriority = earlyStopNotifyPriority;
	}

	public Integer getAutoCreateSuccessPriority() {
		return autoCreateSuccessPriority;
	}

	public void setAutoCreateSuccessPriority(Integer autoCreateSuccessPriority) {
		this.autoCreateSuccessPriority = autoCreateSuccessPriority;
	}

	public Integer getAutoEnableSuccessPriority() {
		return autoEnableSuccessPriority;
	}

	public void setAutoEnableSuccessPriority(Integer autoEnableSuccessPriority) {
		this.autoEnableSuccessPriority = autoEnableSuccessPriority;
	}

	public Integer getAutoDisableSuccessPriority() {
		return autoDisableSuccessPriority;
	}

	public void setAutoDisableSuccessPriority(Integer autoDisableSuccessPriority) {
		this.autoDisableSuccessPriority = autoDisableSuccessPriority;
	}

	public Integer getAutoUpdateSuccessPriority() {
		return autoUpdateSuccessPriority;
	}

	public void setAutoUpdateSuccessPriority(Integer autoUpdateSuccessPriority) {
		this.autoUpdateSuccessPriority = autoUpdateSuccessPriority;
	}

	public Integer getAutoControlFailedPriority() {
		return autoControlFailedPriority;
	}

	public void setAutoControlFailedPriority(Integer autoControlFailedPriority) {
		this.autoControlFailedPriority = autoControlFailedPriority;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public Long getRegDate() {
		return regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
