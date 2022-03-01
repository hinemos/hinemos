/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

public class IntegrationCheckInfoResponse {
	private Integer timeout;
	private Boolean notOrder;
	private String messageOk;
	private String messageNg;
	private List<IntegrationConditionInfoResponse> conditionList = new ArrayList<>();

	public IntegrationCheckInfoResponse() {
	}

	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	public Boolean getNotOrder() {
		return notOrder;
	}
	public void setNotOrder(Boolean notOrder) {
		this.notOrder = notOrder;
	}
	public String getMessageOk() {
		return messageOk;
	}
	public void setMessageOk(String messageOk) {
		this.messageOk = messageOk;
	}
	public String getMessageNg() {
		return messageNg;
	}
	public void setMessageNg(String messageNg) {
		this.messageNg = messageNg;
	}
	public List<IntegrationConditionInfoResponse> getConditionList() {
		return conditionList;
	}
	public void setConditionList(List<IntegrationConditionInfoResponse> conditionList) {
		this.conditionList = conditionList;
	}
	@Override
	public String toString() {
		return "IntegrationCheckInfo [timeout="
				+ timeout + ", notOrder=" + notOrder + ", messageOk=" + messageOk + ", messageNg=" + messageNg
				+ ", conditionList=" + conditionList + "]";
	}

}