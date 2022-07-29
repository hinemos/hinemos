/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class AddJobManualRequest implements RequestDto{

	/** 実行契機ID */
	private String id;

	/** 実行契機名 */
	private String name;

	/** ジョブID */
	private String jobId;

	/** ジョブユニットID */
	private String jobunitId;

	/** オーナーロールID */
	private String ownerRoleId;

	/** 有効/無効 */
	private Boolean valid = false;

	/** カレンダID */
	private String calendarId;

	/** ランタイムジョブ変数情報 */
	private ArrayList<JobRuntimeParamRequest> jobRuntimeParamList;

	public AddJobManualRequest(){
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

	public ArrayList<JobRuntimeParamRequest> getJobRuntimeParamList() {
		return jobRuntimeParamList;
	}

	public void setJobRuntimeParamList(ArrayList<JobRuntimeParamRequest> jobRuntimeParamList) {
		this.jobRuntimeParamList = jobRuntimeParamList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
