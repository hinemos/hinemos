/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeFailureTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeJudgmentTypeEnum;

public abstract class AbstractStringMonitorResponse extends AbstractMonitorResponse {

	@RestBeanConvertEnum
	protected PriorityChangeJudgmentTypeEnum priorityChangeJudgmentType;
	@RestBeanConvertEnum
	protected PriorityChangeFailureTypeEnum priorityChangeFailureType;

	public AbstractStringMonitorResponse() {

	}

	protected Boolean collectorFlg;
	protected String logFormatId;
	protected List<MonitorStringValueInfoResponse> stringValueInfo = new ArrayList<>();
	public Boolean getCollectorFlg() {
		return collectorFlg;
	}
	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}
	public String getLogFormatId() {
		return logFormatId;
	}
	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}
	public List<MonitorStringValueInfoResponse> getStringValueInfo() {
		return stringValueInfo;
	}
	public void setStringValueInfo(List<MonitorStringValueInfoResponse> stringValueInfo) {
		this.stringValueInfo = stringValueInfo;
	}
	public PriorityChangeJudgmentTypeEnum getPriorityChangeJudgmentType() {
		return priorityChangeJudgmentType;
	}
	public void setPriorityChangeJudgmentType(PriorityChangeJudgmentTypeEnum priorityChangeJudgmentType) {
		this.priorityChangeJudgmentType = priorityChangeJudgmentType;
	}
	public PriorityChangeFailureTypeEnum getPriorityChangeFailureType() {
		return priorityChangeFailureType;
	}
	public void setPriorityChangeFailureType(PriorityChangeFailureTypeEnum priorityChangeFailureType) {
		this.priorityChangeFailureType = priorityChangeFailureType;
	}
}
