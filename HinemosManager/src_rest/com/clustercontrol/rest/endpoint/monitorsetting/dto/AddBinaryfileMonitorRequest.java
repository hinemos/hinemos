/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

public class AddBinaryfileMonitorRequest extends AbstractAddBinaryMonitorRequest {

	public AddBinaryfileMonitorRequest() {

	}

	private BinaryCheckInfoRequest binaryCheckInfo;

	public BinaryCheckInfoRequest getBinaryCheckInfo() {
		return binaryCheckInfo;
	}

	public void setBinaryCheckInfo(BinaryCheckInfoRequest binaryCheckInfo) {
		this.binaryCheckInfo = binaryCheckInfo;
	}

	@Override
	public String toString() {
		return "AddBinaryfileMonitorRequest [collectorFlg=" + collectorFlg + ", binaryCheckInfo=" + binaryCheckInfo
				+ ", binaryPatternInfo=" + binaryPatternInfo + ", monitorId=" + monitorId + ", application="
				+ application + ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval="
				+ runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId + ", notifyRelationList="
				+ notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}
}
