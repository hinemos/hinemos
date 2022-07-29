/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class AddSnmptrapMonitorRequest extends AbstractAddMonitorRequest {

	public AddSnmptrapMonitorRequest() {

	}

	private Boolean collectorFlg;
	private String logFormatId;
	private TrapCheckInfoRequest trapCheckInfo;

	public Boolean getCollectorFlg() {
		return collectorFlg;
	}

	public String getLogFormatId() {
		return logFormatId;
	}
	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}

	public TrapCheckInfoRequest getTrapCheckInfo() {
		return trapCheckInfo;
	}

	public void setTrapCheckInfo(TrapCheckInfoRequest trapCheckInfo) {
		this.trapCheckInfo = trapCheckInfo;
	}

	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}

	@Override
	public String toString() {
		return "AddSnmptrapMonitorRequest [collectorFlg=" + collectorFlg + ", logFormatId=" + logFormatId
				+ ", trapCheckInfo=" + trapCheckInfo + ", monitorId=" + monitorId + ", application=" + application
				+ ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval
				+ ", calendarId=" + calendarId + ", facilityId=" + facilityId + ", notifyRelationList="
				+ notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

}
