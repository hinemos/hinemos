/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.dto.RequestDto;

public class GetPlanListRequest implements RequestDto {

	/** 開始 */
	@RestBeanConvertDatetime
	private String fromDate;

	/** 終了*/
	@RestBeanConvertDatetime
	private String toDate;

	/** 実行契機ID */
	private String jobKickId;

	// 取得されるレコードの上限数
	@RestValidateInteger(notNull = false,minVal=0)
	private Integer size ;

	public GetPlanListRequest(){
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public String getJobKickId() {
		return jobKickId;
	}

	public void setJobKickId(String jobKickId) {
		this.jobKickId = jobKickId;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
