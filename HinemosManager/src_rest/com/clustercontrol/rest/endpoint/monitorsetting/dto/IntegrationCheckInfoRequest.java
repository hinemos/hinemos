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

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class IntegrationCheckInfoRequest implements RequestDto {
	private Integer timeout;
	private Boolean notOrder;
	private String messageOk;
	private String messageNg;
	private List<IntegrationConditionInfoRequest> conditionList = new ArrayList<>();
	public IntegrationCheckInfoRequest() {
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
	public List<IntegrationConditionInfoRequest> getConditionList() {
		return conditionList;
	}
	public void setConditionList(List<IntegrationConditionInfoRequest> conditionList) {
		this.conditionList = conditionList;
	}
	@Override
	public String toString() {
		return "IntegrationCheckInfo [timeout="
				+ timeout + ", notOrder=" + notOrder + ", messageOk=" + messageOk + ", messageNg=" + messageNg
				+ ", conditionList=" + conditionList + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}