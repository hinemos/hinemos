/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobRuntimeParamTypeEnum;

public class JobRuntimeParamResponse {
	/** 名前 */
	private String paramId;
	/** 種別 */
	@RestBeanConvertEnum
	private JobRuntimeParamTypeEnum paramType;
	/** 値（デフォルト値） */
	private String value;
	/** 説明 */
	private String description;
	/** 必須フラグ */
	private Boolean requiredFlg = false;
	/** ランタイムジョブ変数詳細情報 */
	private ArrayList<JobRuntimeParamDetailResponse> jobRuntimeParamDetailList;

	public JobRuntimeParamResponse(){
	}

	public String getParamId() {
		return paramId;
	}
	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	public JobRuntimeParamTypeEnum getParamType() {
		return paramType;
	}
	public void setParamType(JobRuntimeParamTypeEnum paramType) {
		this.paramType = paramType;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getRequiredFlg() {
		return requiredFlg;
	}
	public void setRequiredFlg(Boolean requiredFlg) {
		this.requiredFlg = requiredFlg;
	}

	public ArrayList<JobRuntimeParamDetailResponse> getJobRuntimeParamDetailList() {
		return jobRuntimeParamDetailList;
	}
	public void setJobRuntimeParamDetailList(ArrayList<JobRuntimeParamDetailResponse> jobRuntimeParamDetailList) {
		this.jobRuntimeParamDetailList = jobRuntimeParamDetailList;
	}
}
