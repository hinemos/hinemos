/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ControlEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusEnum;
import com.clustercontrol.util.MessageConstant;

public class JobOperationRequest implements RequestDto {

	public JobOperationRequest() {
	}

	@RestItemName(value=MessageConstant.CONTROL)
	@RestValidateObject(notNull=true)
	@RestBeanConvertEnum
	private ControlEnum control;
	@RestBeanConvertEnum
	private EndStatusEnum endStatus;
	private Integer endValue;
	
	public ControlEnum getControl() {
		return control;
	}
	public void setControl(ControlEnum control) {
		this.control = control;
	}
	public EndStatusEnum getEndStatus() {
		return endStatus;
	}
	public void setEndStatus(EndStatusEnum endStatus) {
		this.endStatus = endStatus;
	}
	public Integer getEndValue() {
		return endValue;
	}
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
