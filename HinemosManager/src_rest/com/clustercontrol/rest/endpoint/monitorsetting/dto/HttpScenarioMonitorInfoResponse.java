/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityChangeJudgmentTypeEnum;

public class HttpScenarioMonitorInfoResponse extends AbstractMonitorResponse {

	private Boolean collectorFlg;
	private String itemName;
	private String measure;
	private HttpScenarioCheckInfoResponse httpScenarioCheckInfo;
	@RestBeanConvertEnum
	private PriorityChangeJudgmentTypeEnum priorityChangeJudgmentType;

	public HttpScenarioMonitorInfoResponse() {
	}

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
	public HttpScenarioCheckInfoResponse getHttpScenarioCheckInfo() {
		return httpScenarioCheckInfo;
	}
	public void setHttpScenarioCheckInfo(HttpScenarioCheckInfoResponse httpScenarioCheckInfo) {
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
		return "HttpScenarioMonitorInfoResponse [collectorFlg=" + collectorFlg + ", itemName=" + itemName + ", measure="
				+ measure + ", httpScenarioCheckInfo=" + httpScenarioCheckInfo + ", monitorId=" + monitorId
				+ ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", facilityId=" + facilityId + ", priorityChangeJudgmentType="
				+ priorityChangeJudgmentType + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId="
				+ ownerRoleId + "]";
	}
}