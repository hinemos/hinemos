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

public class ModifyJobManualRequest implements RequestDto {

	/** 実行契機名 */
	private String name;

	/** ジョブID */
	private String jobId;

	/** ジョブユニットID */
	private String jobunitId;

	/** 有効/無効 */
	private Boolean valid = false;

	/** カレンダID */
	private String calendarId;

	/** ランタイムジョブ変数情報 */
	private ArrayList<JobRuntimeParamRequest> jobRuntimeParamList;

	public ModifyJobManualRequest() {
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
