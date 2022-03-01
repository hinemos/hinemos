/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.grafana.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class GetJobLastRunTimeListRequest implements RequestDto {

	public GetJobLastRunTimeListRequest() {
	}

	@RestItemName(value = MessageConstant.JOB_LAST_RUN_TIME_JOBKICK_IDS)
	@RestValidateCollection(notNull = false)
	private List<String> jobKickIds = new ArrayList<String>();

	@RestItemName(MessageConstant.JOB_START_DATE_FROM)
	private String startDateFrom;

	@RestItemName(MessageConstant.JOB_START_DATE_TO)
	private String startDateTo;

	@RestItemName(MessageConstant.JOB_END_DATE_FROM)
	private String endDateFrom;

	@RestItemName(MessageConstant.JOB_END_DATE_TO)
	private String endDateTo;

	@RestItemName(value = MessageConstant.JOB_LAST_RUN_TIME_SIZE)
	@RestValidateInteger(notNull = false, minVal = 0)
	private Integer size;

	public List<String> getJobKickIds() {
		return jobKickIds;
	}
	
	public void setJobKickIds(List<String> jobKickIds) {
		this.jobKickIds = jobKickIds;
	}

	public String getStartDateFrom() {
		return startDateFrom;
	}

	public void setStartDateFrom(String startDateFrom) {
		this.startDateFrom = startDateFrom;
	}

	public String getStartDateTo() {
		return startDateTo;
	}

	public void setStartDateTo(String startDateTo) {
		this.startDateTo = startDateTo;
	}

	public String getEndDateFrom() {
		return endDateFrom;
	}

	public void setEndDateFrom(String endDateFrom) {
		this.endDateFrom = endDateFrom;
	}

	public String getEndDateTo() {
		return endDateTo;
	}

	public void setEndDateTo(String endDateTo) {
		this.endDateTo = endDateTo;
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
