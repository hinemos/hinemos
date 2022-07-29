/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rpa.util.RpaUtil;
import com.clustercontrol.util.MessageConstant;

public class ModifyRpaManagementToolMonitorRequest extends AbstractModifyTruthMonitorRequest {

	public ModifyRpaManagementToolMonitorRequest() {

	}

	private RpaManagementToolServiceCheckInfoRequest rpaManagementToolServiceCheckInfo;

	public RpaManagementToolServiceCheckInfoRequest getRpaManagementToolServiceCheckInfo() {
		return rpaManagementToolServiceCheckInfo;
	}

	public void setRpaManagementToolServiceCheckInfo(RpaManagementToolServiceCheckInfoRequest rpaManagementToolServiceCheckInfo) {
		this.rpaManagementToolServiceCheckInfo = rpaManagementToolServiceCheckInfo;
	}

	@Override
	public String toString() {
		return "ModifyRpaManagementToolServiceMonitorRequest [rpaManagementToolServiceCheckInfo=" + rpaManagementToolServiceCheckInfo + ", truthValueInfo="
				+ truthValueInfo + ", application=" + application + ", description=" + description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval
				+ ", calendarId=" + calendarId + ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList + "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
		super.correlationCheck();
		// 対象ファシリティIDがRPA管理ツールサービスを表すスコープのIDであることをチェックする。
		if (!RpaUtil.checkRpaScope(getFacilityId())) {
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_RPA_MANAGEMENT_TOOL_ACCOUNT_SCOPE.getMessage());
		}
		rpaManagementToolServiceCheckInfo.correlationCheck();
	}
}
