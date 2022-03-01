/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class SnmpStringMonitorInfoResponse extends AbstractStringMonitorResponse {

	private SnmpCheckInfoResponse snmpCheckInfo;

	public SnmpStringMonitorInfoResponse() {
	}

	public SnmpCheckInfoResponse getSnmpCheckInfo() {
		return snmpCheckInfo;
	}

	public void setSnmpCheckInfo(SnmpCheckInfoResponse snmpCheckInfo) {
		this.snmpCheckInfo = snmpCheckInfo;
	}

	@Override
	public String toString() {
		return "SnmpStringMonitorInfoResponse [snmpCheckInfo=" + snmpCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId + ", application=" + application
				+ ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval
				+ ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId="
				+ ownerRoleId + "]";
	}

	
}