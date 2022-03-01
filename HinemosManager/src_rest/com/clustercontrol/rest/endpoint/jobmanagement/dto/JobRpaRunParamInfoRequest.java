/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.RpaJobRunParamInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.dto.RequestDto;

@RestBeanConvertAssertion(to = RpaJobRunParamInfo.class)
public class JobRpaRunParamInfoRequest implements RequestDto {
	/** パラメータID */
	private Integer paramId;
	/** パラメータの値 */
	private String paramValue;

	public JobRpaRunParamInfoRequest() {
	}

	/**
	 * @return the paramId
	 */
	public Integer getParamId() {
		return paramId;
	}

	/**
	 * @param paramId the paramId to set
	 */
	public void setParamId(Integer paramId) {
		this.paramId = paramId;
	}

	/**
	 * @return the paramValue
	 */
	public String getParamValue() {
		return paramValue;
	}

	/**
	 * @param paramValue
	 *            the paramValue to set
	 */
	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
