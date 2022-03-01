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
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusEnum;

public class JobEndStatusInfoRequest implements RequestDto {
	
	/** 終了状態の種別 */
	@RestBeanConvertEnum
	private EndStatusEnum type = EndStatusEnum.NORMAL;

	/** 終了状態の終了値 */
	private Integer value = 0;

	/** 終了値範囲(開始) */
	private Integer startRangeValue = 0;

	/** 終了値範囲(終了) */
	private Integer endRangeValue = 0;


	public JobEndStatusInfoRequest() {
	}


	public EndStatusEnum getType() {
		return type;
	}


	public void setType(EndStatusEnum type) {
		this.type = type;
	}


	public Integer getValue() {
		return value;
	}


	public void setValue(Integer value) {
		this.value = value;
	}


	public Integer getStartRangeValue() {
		return startRangeValue;
	}


	public void setStartRangeValue(Integer startRangeValue) {
		this.startRangeValue = startRangeValue;
	}


	public Integer getEndRangeValue() {
		return endRangeValue;
	}


	public void setEndRangeValue(Integer endRangeValue) {
		this.endRangeValue = endRangeValue;
	}


	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
