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

public class ModifyHttpScenarioMonitorRequest extends AbstractModifyMonitorRequest {

	@RestItemName(value = MessageConstant.PRIORITY_CHANGE_JUDGMENT_TYPE)
	@RestValidateObject(notNull = true)
	@RestBeanConvertEnum
	private PriorityChangeJudgmentTypeEnum priorityChangeJudgmentType;

	public ModifyHttpScenarioMonitorRequest() {

	}

	private Boolean collectorFlg;
	private String itemName;
	private String measure;
	private HttpScenarioCheckInfoRequest httpScenarioCheckInfo;

	
	public Boolean getCollectorFlg() {
		return collectorFlg;
	}


	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}


	public String getItemName() {
		return itemName;
	}


	public void setItemName(String itemName) {
		this.itemName = itemName;
	}


	public String getMeasure() {
		return measure;
	}


	public void setMeasure(String measure) {
		this.measure = measure;
	}


	public HttpScenarioCheckInfoRequest getHttpScenarioCheckInfo() {
		return httpScenarioCheckInfo;
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
		return "ModifyHttpScenarioMonitorRequest [collectorFlg=" + collectorFlg + ", itemName=" + itemName
				+ ", measure=" + measure + ", httpScenarioCheckInfo=" + httpScenarioCheckInfo + ", application="
				+ application + ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval="
				+ runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId
				+ ", priorityChangeJudgmentType=" + priorityChangeJudgmentType + ", notifyRelationList="
				+ notifyRelationList + "]";
	}
}
