/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobParamTypeEnum;

/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Infoクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class JobParameterInfoRequest implements RequestDto {

	/** パラメータID */
	private String paramId;

	/** パラメータ種別 */
	@RestBeanConvertEnum
	private JobParamTypeEnum type = JobParamTypeEnum.SYSTEM_JOB;

	/** 説明 */
	private String description;

	/** 値 */
	private String value;


	public JobParameterInfoRequest() {
	}



	public String getParamId() {
		return paramId;
	}



	public void setParamId(String paramId) {
		this.paramId = paramId;
	}



	public JobParamTypeEnum getType() {
		return type;
	}



	public void setType(JobParamTypeEnum type) {
		this.type = type;
	}



	public String getDescription() {
		return description;
	}



	public void setDescription(String description) {
		this.description = description;
	}



	public String getValue() {
		return value;
	}



	public void setValue(String value) {
		this.value = value;
	}



	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
