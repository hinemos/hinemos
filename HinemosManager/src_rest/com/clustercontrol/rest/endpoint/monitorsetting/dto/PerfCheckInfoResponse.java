/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class PerfCheckInfoResponse {
	private Boolean breakdownFlg;
	private String deviceDisplayName;
	private String itemCode;

	public PerfCheckInfoResponse() {
	}

	public Boolean getBreakdownFlg() {
		return breakdownFlg;
	}
	public void setBreakdownFlg(Boolean breakdownFlg) {
		this.breakdownFlg = breakdownFlg;
	}
	public String getDeviceDisplayName() {
		return deviceDisplayName;
	}
	public void setDeviceDisplayName(String deviceDisplayName) {
		this.deviceDisplayName = deviceDisplayName;
	}
	public String getItemCode() {
		return itemCode;
	}
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	@Override
	public String toString() {
		return "PerfCheckInfo [breakdownFlg="
				+ breakdownFlg + ", deviceDisplayName=" + deviceDisplayName + ", itemCode=" + itemCode + "]";
	}

}