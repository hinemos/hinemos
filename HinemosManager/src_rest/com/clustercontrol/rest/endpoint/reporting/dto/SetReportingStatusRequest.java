/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class SetReportingStatusRequest implements RequestDto {
	
	public SetReportingStatusRequest (){}
	
	private List<String> reportIdList;
	private Boolean validFlg;
	
	public List<String> getReportIdList() {
		return reportIdList;
	}

	public void setReportIdList(List<String> reportIdList) {
		this.reportIdList = reportIdList;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@Override
	public String toString() {
		return "SetReportingStatusRequest [reportingIdList=" + reportIdList + ", validFlg=" + validFlg + "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
