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
public class ModifySnmptrapMonitorRequest extends AbstractModifyMonitorRequest {

	public ModifySnmptrapMonitorRequest() {
		// runIntervalは、固定値にする
		super.setRunInterval(RunIntervalEnum.NONE);
	}

	private Boolean collectorFlg;
	private String logFormatId;
	private TrapCheckInfoRequest trapCheckInfo;

	@Override
	public void setRunInterval(RunIntervalEnum runInterval) {
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


	public TrapCheckInfoRequest getTrapCheckInfo() {
		return trapCheckInfo;
	}


	public void setTrapCheckInfo(TrapCheckInfoRequest trapCheckInfo) {
		this.trapCheckInfo = trapCheckInfo;
	}

	@Override
	public String toString() {
		return "ModifySnmptrapMonitorRequest [collectorFlg=" + collectorFlg + ", logFormatId=" + logFormatId
				+ ", trapCheckInfo=" + trapCheckInfo + ", application=" + application + ", description=" + description
				+ ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", calendarId=" + calendarId
				+ ", facilityId=" + facilityId + ", notifyRelationList=" + notifyRelationList + "]";
	}
}
