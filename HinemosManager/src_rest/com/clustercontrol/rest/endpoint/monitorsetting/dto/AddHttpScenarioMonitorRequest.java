/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeJudgmentTypeEnum;
import com.clustercontrol.util.MessageConstant;

public class AddHttpScenarioMonitorRequest extends AbstractAddMonitorRequest {

	@RestItemName(value = MessageConstant.PRIORITY_CHANGE_JUDGMENT_TYPE)
	@RestValidateObject(notNull = true)
	@RestBeanConvertEnum
	protected PriorityChangeJudgmentTypeEnum priorityChangeJudgmentType;

	public AddHttpScenarioMonitorRequest() {

	}

	private Boolean collectorFlg;
	private String itemName;
	private String measure;
	private HttpScenarioCheckInfoRequest httpScenarioCheckInfo;

	public Boolean getCollectorFlg() {
		return collectorFlg;
	}

	public String getItemName() {
		return itemName;
	}

	public String getMeasure() {
		return measure;
	}

	public HttpScenarioCheckInfoRequest getHttpScenarioCheckInfo() {
		return httpScenarioCheckInfo;
	}

	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}

	public void setHttpScenarioCheckInfo(HttpScenarioCheckInfoRequest httpScenarioCheckInfo) {
		this.httpScenarioCheckInfo = httpScenarioCheckInfo;
	}

	public PriorityChangeJudgmentTypeEnum getPriorityChangeJudgmentType() {
		return priorityChangeJudgmentType;
	}

	public void setPriorityChangeJudgmentType(PriorityChangeJudgmentTypeEnum priorityChangeJudgmentType) {
		this.priorityChangeJudgmentType = priorityChangeJudgmentType;
	}

	@Override
	public String toString() {
		return "AddHttpScenarioMonitorRequest [collectorFlg=" + collectorFlg + ", itemName=" + itemName + ", measure="
				+ measure + ", httpScenarioCheckInfo=" + httpScenarioCheckInfo + ", monitorId=" + monitorId
				+ ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId
				+ ", priorityChangeJudgmentType=" + priorityChangeJudgmentType + ", notifyRelationList="
				+ notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

}
