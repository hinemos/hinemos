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
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobRuntimeParamTypeEnum;
import com.clustercontrol.util.MessageConstant;

public class JobRuntimeParamRequest implements RequestDto {

	public JobRuntimeParamRequest() {
	}

	/** 名前 */
	private String paramId;
	/** 種別 */
	@RestItemName(value=MessageConstant.JOB_PARAM_TYPE)
	@RestValidateObject(notNull=true)
	@RestBeanConvertEnum
	private JobRuntimeParamTypeEnum paramType;
	/** 値（デフォルト値） */
	private String value;
	/** 説明 */
	private String description;
	/** 必須フラグ */
	@RestItemName(value=MessageConstant.REQUIRED_FLAG)
	@RestValidateObject(notNull=true)
	private Boolean requiredFlg ;
	/** ランタイムジョブ変数詳細情報 */
	private ArrayList<JobRuntimeParamDetailRequest> jobRuntimeParamDetailList;


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

	public ArrayList<JobRuntimeParamDetailRequest> getJobRuntimeParamDetailList() {
		return jobRuntimeParamDetailList;
	}
	public void setJobRuntimeParamDetailList(ArrayList<JobRuntimeParamDetailRequest> jobRuntimeParamDetailList) {
		this.jobRuntimeParamDetailList = jobRuntimeParamDetailList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
