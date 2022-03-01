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

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;

public class TrapCheckInfoResponse {
	private Boolean charsetConvert;
	private String charsetName;
	private Boolean communityCheck;
	private String communityName;
	private Boolean notifyofReceivingUnspecifiedFlg;
	@RestBeanConvertEnum
	private PriorityEnum priorityUnspecified;
	private List<TrapValueInfoResponse> monitorTrapValueInfoEntities = new ArrayList<>();

	public TrapCheckInfoResponse() {
	}

	public Boolean getCharsetConvert() {
		return charsetConvert;
	}
	public void setCharsetConvert(Boolean charsetConvert) {
		this.charsetConvert = charsetConvert;
	}
	public String getCharsetName() {
		return charsetName;
	}
	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}
	public Boolean getCommunityCheck() {
		return communityCheck;
	}
	public void setCommunityCheck(Boolean communityCheck) {
		this.communityCheck = communityCheck;
	}
	public String getCommunityName() {
		return communityName;
	}
	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}
	public Boolean getNotifyofReceivingUnspecifiedFlg() {
		return notifyofReceivingUnspecifiedFlg;
	}
	public void setNotifyofReceivingUnspecifiedFlg(Boolean notifyofReceivingUnspecifiedFlg) {
		this.notifyofReceivingUnspecifiedFlg = notifyofReceivingUnspecifiedFlg;
	}
	public PriorityEnum getPriorityUnspecified() {
		return priorityUnspecified;
	}
	public void setPriorityUnspecified(PriorityEnum priorityUnspecified) {
		this.priorityUnspecified = priorityUnspecified;
	}
	public List<TrapValueInfoResponse> getMonitorTrapValueInfoEntities() {
		return monitorTrapValueInfoEntities;
	}
	public void setMonitorTrapValueInfoEntities(List<TrapValueInfoResponse> monitorTrapValueInfoEntities) {
		this.monitorTrapValueInfoEntities = monitorTrapValueInfoEntities;
	}
	@Override
	public String toString() {
		return "TrapCheckInfo [charsetConvert=" + charsetConvert + ", charsetName=" + charsetName + ", communityCheck="
				+ communityCheck + ", communityName=" + communityName + ", notifyofReceivingUnspecifiedFlg="
				+ notifyofReceivingUnspecifiedFlg + ", priorityUnspecified=" + priorityUnspecified
				+ ", monitorTrapValueInfoEntities=" + monitorTrapValueInfoEntities + "]";
	}

}