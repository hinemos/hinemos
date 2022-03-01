/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;

public class JobLinkRcvResponse {

	/** 実行契機ID */
	private String id;

	/** 実行契機名 */
	private String name;

	/** ジョブID */
	private String jobId;

	/** ジョブ名 */
	private String jobName;

	/** ジョブユニットID */
	private String jobunitId;

	/** オーナーロールID */
	private String ownerRoleId;

	/** 新規作成ユーザ */
	private String createUser;

	/** 作成日時 */
	@RestBeanConvertDatetime
	private String createTime;

	/** 最新更新ユーザ */
	private String updateUser;

	/** 最新更新日時 */
	@RestBeanConvertDatetime
	private String updateTime;

	/** 有効/無効 */
	private Boolean valid = false;

	/** カレンダID */
	private String calendarId;

	/** ランタイムジョブ変数情報 */
	private ArrayList<JobRuntimeParamResponse> jobRuntimeParamList;

	/** ジョブ連携受信 */
	private String joblinkMessageId;

	private String facilityId;

	private String scope;

	private Boolean infoValidFlg;

	private Boolean warnValidFlg;

	private Boolean criticalValidFlg;

	private Boolean unknownValidFlg;

	private Boolean applicationFlg;

	private String application;

	private Boolean monitorDetailIdFlg;

	private String monitorDetailId;

	private Boolean messageFlg;

	private String message;

	private Boolean expFlg;

	private ArrayList<JobLinkExpInfoResponse> jobLinkExpList;

	public JobLinkRcvResponse() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public ArrayList<JobRuntimeParamResponse> getJobRuntimeParamList() {
		return jobRuntimeParamList;
	}

	public void setJobRuntimeParamList(ArrayList<JobRuntimeParamResponse> jobRuntimeParamList) {
		this.jobRuntimeParamList = jobRuntimeParamList;
	}

	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
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

	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}

	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}

	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}

	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}

	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	public Boolean getApplicationFlg() {
		return applicationFlg;
	}

	public void setApplicationFlg(Boolean applicationFlg) {
		this.applicationFlg = applicationFlg;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Boolean getMonitorDetailIdFlg() {
		return monitorDetailIdFlg;
	}

	public void setMonitorDetailIdFlg(Boolean monitorDetailIdFlg) {
		this.monitorDetailIdFlg = monitorDetailIdFlg;
	}

	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	public Boolean getMessageFlg() {
		return messageFlg;
	}

	public void setMessageFlg(Boolean messageFlg) {
		this.messageFlg = messageFlg;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getExpFlg() {
		return expFlg;
	}

	public void setExpFlg(Boolean expFlg) {
		this.expFlg = expFlg;
	}

	public ArrayList<JobLinkExpInfoResponse> getJobLinkExpList() {
		return jobLinkExpList;
	}

	public void setJobLinkExpList(ArrayList<JobLinkExpInfoResponse> jobLinkExpList) {
		this.jobLinkExpList = jobLinkExpList;
	}
}
