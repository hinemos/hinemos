/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class HttpStringMonitorInfoResponse extends AbstractStringMonitorResponse {

	private HttpCheckInfoResponse httpCheckInfo;

	public HttpStringMonitorInfoResponse() {
	}

	public HttpCheckInfoResponse getHttpCheckInfo() {
		return httpCheckInfo;
	}

	public void setHttpCheckInfo(HttpCheckInfoResponse httpCheckInfo) {
		this.httpCheckInfo = httpCheckInfo;
	}

	@Override
	public String toString() {
		return "HttpStringMonitorInfoResponse [httpCheckInfo=" + httpCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId + ", application=" + application
				+ ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval
				+ ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId="
				+ ownerRoleId + "]";
	}

	
}