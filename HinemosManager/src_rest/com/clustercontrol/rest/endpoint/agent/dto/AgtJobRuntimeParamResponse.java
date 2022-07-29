/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.ArrayList;

import com.clustercontrol.jobmanagement.bean.JobRuntimeParam;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = JobRuntimeParam.class)
public class AgtJobRuntimeParamResponse {

	// ---- JobRuntimeParam
	private String paramId;
	private Integer paramType;
	private String value;
	private String description;
	private Boolean requiredFlg;
	private ArrayList<AgtJobRuntimeParamDetailResponse> jobRuntimeParamDetailList;

	public AgtJobRuntimeParamResponse() {
	}

	// ---- accessors

	public String getParamId() {
		return paramId;
	}

	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	public Integer getParamType() {
		return paramType;
	}

	public void setParamType(Integer paramType) {
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

	public ArrayList<AgtJobRuntimeParamDetailResponse> getJobRuntimeParamDetailList() {
		return jobRuntimeParamDetailList;
	}

	public void setJobRuntimeParamDetailList(ArrayList<AgtJobRuntimeParamDetailResponse> jobRuntimeParamDetailList) {
		this.jobRuntimeParamDetailList = jobRuntimeParamDetailList;
	}
}
