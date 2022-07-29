/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.ArrayList;

import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = JobFileCheck.class)
public class AgtJobFileCheckRequest extends AgentRequestDto {

	// ---- from JobKick
	private Integer type;
	// private String id;
	private String name;
	private String jobId;
	private String jobName;
	private String jobunitId;
	private String ownerRoleId;
	private String createUser;
	private Long createTime;
	private String updateUser;
	private Long updateTime;
	private Boolean valid;
	private String calendarId;
	private AgtCalendarInfoResponse calendarInfo;
	private ArrayList<AgtJobRuntimeParamResponse> jobRuntimeParamList;

	// ---- from JobFileCheck
	private String facilityId;
	private String scope;
	private String directory;
	private String fileName;
	private Integer eventType;
	private Integer modifyType;
	private Boolean carryOverJudgmentFlg;

	public AgtJobFileCheckRequest() {
	}

	// ---- accessors

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
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

	public Boolean getValid() {
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

	public AgtCalendarInfoResponse getCalendarInfo() {
		return calendarInfo;
	}

	public void setCalendarInfo(AgtCalendarInfoResponse calendarInfo) {
		this.calendarInfo = calendarInfo;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Long updateTime) {
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

	public ArrayList<AgtJobRuntimeParamResponse> getJobRuntimeParamList() {
		return jobRuntimeParamList;
	}

	public void setJobRuntimeParamList(ArrayList<AgtJobRuntimeParamResponse> jobRuntimeParamList) {
		this.jobRuntimeParamList = jobRuntimeParamList;
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

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getEventType() {
		return eventType;
	}

	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}

	public Integer getModifyType() {
		return modifyType;
	}

	public void setModifyType(Integer modifyType) {
		this.modifyType = modifyType;
	}

	public Boolean getCarryOverJudgmentFlg() {
		return carryOverJudgmentFlg;
	}

	public void setCarryOverJudgmentFlg(Boolean carryOverJudgmentFlg) {
		this.carryOverJudgmentFlg = carryOverJudgmentFlg;
	}

}
