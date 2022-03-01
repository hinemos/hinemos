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

import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeJudgmentTypeEnum;
import com.clustercontrol.util.MessageConstant;

public abstract class AbstractModifyStringMonitorRequest extends AbstractModifyMonitorRequest {

	@RestItemName(value = MessageConstant.PRIORITY_CHANGE_JUDGMENT_TYPE)
	@RestValidateObject(notNull = true)
	@RestBeanConvertEnum
	protected PriorityChangeJudgmentTypeEnum priorityChangeJudgmentType;

	public AbstractModifyStringMonitorRequest() {

	}

	protected Boolean collectorFlg;
	protected String logFormatId;
	protected List<MonitorStringValueInfoRequest> stringValueInfo = new ArrayList<>();
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
	public List<MonitorStringValueInfoRequest> getStringValueInfo() {
		return stringValueInfo;
	}
	public void setStringValueInfo(List<MonitorStringValueInfoRequest> stringValueInfo) {
		this.stringValueInfo = stringValueInfo;
	}

	public PriorityChangeJudgmentTypeEnum getPriorityChangeJudgmentType() {
		return priorityChangeJudgmentType;
	}

	public void setPriorityChangeJudgmentType(PriorityChangeJudgmentTypeEnum priorityChangeJudgmentType) {
		this.priorityChangeJudgmentType = priorityChangeJudgmentType;
	}
	
}
