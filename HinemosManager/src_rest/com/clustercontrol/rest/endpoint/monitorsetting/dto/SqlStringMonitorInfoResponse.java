/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;


public class SqlStringMonitorInfoResponse extends AbstractStringMonitorResponse {

	private SqlCheckInfoResponse sqlCheckInfo;

	public SqlStringMonitorInfoResponse() {
	}

	public SqlCheckInfoResponse getSqlCheckInfo() {
		return sqlCheckInfo;
	}

	public void setSqlCheckInfo(SqlCheckInfoResponse sqlCheckInfo) {
		this.sqlCheckInfo = sqlCheckInfo;
	}

	@Override
	public String toString() {
		return "SqlStringMonitorInfoResponse [sqlCheckInfo=" + sqlCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId + ", application=" + application
				+ ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval
				+ ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList + ", ownerRoleId="
				+ ownerRoleId + "]";
	}

}