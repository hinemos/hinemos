/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class SnmptrapMonitorInfoResponse extends AbstractMonitorResponse {

	private Boolean collectorFlg;
	private String logFormatId;
	private TrapCheckInfoResponse trapCheckInfo;

	public SnmptrapMonitorInfoResponse() {
	}

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
	public TrapCheckInfoResponse getTrapCheckInfo() {
		return trapCheckInfo;
	}
	public void getTrapCheckInfo(TrapCheckInfoResponse trapCheckInfo) {
		this.trapCheckInfo = trapCheckInfo;
	}
	@Override
	public String toString() {
		return "SnmptrapMonitorInfoResponse [collectorFlg=" + collectorFlg + ", logFormatId=" + logFormatId
				+ ", trapCheckInfo=" + trapCheckInfo + ", monitorId=" + monitorId + ", application="
				+ application + ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval="
				+ runInterval + ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList
				+ ", ownerRoleId=" + ownerRoleId + "]";
	}

	
}