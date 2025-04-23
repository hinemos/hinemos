/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.RunIntervalEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"runInterval"})
public class AddWineventMonitorRequest extends AbstractAddStringMonitorRequest {

	public AddWineventMonitorRequest() {
		// runIntervalは、固定値にする
		super.setRunInterval(RunIntervalEnum.NONE);
	}

	private WinEventCheckInfoRequest winEventCheckInfo;

	@Override
	public void setRunInterval(RunIntervalEnum runInterval) {
	}

	public WinEventCheckInfoRequest getWinEventCheckInfo() {
		return winEventCheckInfo;
	}

	public void setWinEventCheckInfo(WinEventCheckInfoRequest winEventCheckInfo) {
		this.winEventCheckInfo = winEventCheckInfo;
	}

	@Override
	public String toString() {
		return "AddWineventMonitorRequest [winEventCheckInfo=" + winEventCheckInfo + ", collectorFlg=" + collectorFlg
				+ ", logFormatId=" + logFormatId + ", stringValueInfo=" + stringValueInfo + ", monitorId=" + monitorId
				+ ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg
				+ ", runInterval=" + runInterval + ", calendarId=" + calendarId + ", facilityId=" + facilityId
				+ ", notifyRelationList=" + notifyRelationList + ", ownerRoleId=" + ownerRoleId + "]";
	}

}
