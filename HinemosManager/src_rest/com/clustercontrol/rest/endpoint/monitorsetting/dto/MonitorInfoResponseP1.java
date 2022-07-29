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

public class MonitorInfoResponseP1 extends AbstractMonitorResponse {

	private Boolean collectorFlg;
	private CustomCheckInfoResponse customCheckInfo;
	private CustomTrapCheckInfoResponse customTrapCheckInfo;
	private HttpCheckInfoResponse httpCheckInfo;
	private SnmpCheckInfoResponse snmpCheckInfo;
	private SqlCheckInfoResponse sqlCheckInfo;
	private WinEventCheckInfoResponse winEventCheckInfo;
	private LogfileCheckInfoResponse logfileCheckInfo;
	private RpaLogFileCheckInfoResponse rpaLogFileCheckInfo;
	private PluginCheckInfoResponse pluginCheckInfo;
	private List<MonitorStringValueInfoResponse> stringValueInfo = new ArrayList<>();
	private String logFormatId;

	public MonitorInfoResponseP1() {
	}

	public Boolean getCollectorFlg() {
		return collectorFlg;
	}


	public void setCollectorFlg(Boolean collectorFlg) {
		this.collectorFlg = collectorFlg;
	}


	public CustomCheckInfoResponse getCustomCheckInfo() {
		return customCheckInfo;
	}


	public void setCustomCheckInfo(CustomCheckInfoResponse customCheckInfo) {
		this.customCheckInfo = customCheckInfo;
	}


	public CustomTrapCheckInfoResponse getCustomTrapCheckInfo() {
		return customTrapCheckInfo;
	}


	public void setCustomTrapCheckInfo(CustomTrapCheckInfoResponse customTrapCheckInfo) {
		this.customTrapCheckInfo = customTrapCheckInfo;
	}


	public HttpCheckInfoResponse getHttpCheckInfo() {
		return httpCheckInfo;
	}


	public void setHttpCheckInfo(HttpCheckInfoResponse httpCheckInfo) {
		this.httpCheckInfo = httpCheckInfo;
	}


	public SnmpCheckInfoResponse getSnmpCheckInfo() {
		return snmpCheckInfo;
	}


	public void setSnmpCheckInfo(SnmpCheckInfoResponse snmpCheckInfo) {
		this.snmpCheckInfo = snmpCheckInfo;
	}


	public SqlCheckInfoResponse getSqlCheckInfo() {
		return sqlCheckInfo;
	}


	public void setSqlCheckInfo(SqlCheckInfoResponse sqlCheckInfo) {
		this.sqlCheckInfo = sqlCheckInfo;
	}


	public WinEventCheckInfoResponse getWinEventCheckInfo() {
		return winEventCheckInfo;
	}


	public void setWinEventCheckInfo(WinEventCheckInfoResponse winEventCheckInfo) {
		this.winEventCheckInfo = winEventCheckInfo;
	}


	public LogfileCheckInfoResponse getLogfileCheckInfo() {
		return logfileCheckInfo;
	}


	public void setLogfileCheckInfo(LogfileCheckInfoResponse logfileCheckInfo) {
		this.logfileCheckInfo = logfileCheckInfo;
	}


	public RpaLogFileCheckInfoResponse getRpaLogFileCheckInfo() {
		return rpaLogFileCheckInfo;
	}

	public void setRpaLogFileCheckInfo(RpaLogFileCheckInfoResponse rpaLogFileCheckInfo) {
		this.rpaLogFileCheckInfo = rpaLogFileCheckInfo;
	}

	public PluginCheckInfoResponse getPluginCheckInfo() {
		return pluginCheckInfo;
	}


	public void setPluginCheckInfo(PluginCheckInfoResponse pluginCheckInfo) {
		this.pluginCheckInfo = pluginCheckInfo;
	}


	public List<MonitorStringValueInfoResponse> getStringValueInfo() {
		return stringValueInfo;
	}


	public void setStringValueInfo(List<MonitorStringValueInfoResponse> stringValueInfo) {
		this.stringValueInfo = stringValueInfo;
	}


	public String getLogFormatId() {
		return logFormatId;
	}


	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}


	@Override
	public String toString() {
		return "MonitorInfoResponseP1 [monitorId=" + monitorId + ", monitorType=" + monitorType + ", monitorTypeId="
				+ monitorTypeId + ", application=" + application + ", collectorFlg=" + collectorFlg + ", description="
				+ description + ", monitorFlg=" + monitorFlg + ", runInterval=" + runInterval + ", calendarId=" + calendarId
				+ ", customCheckInfo=" + customCheckInfo + ", customTrapCheckInfo=" + customTrapCheckInfo
				+ ", httpCheckInfo=" + httpCheckInfo + ", facilityId=" + facilityId + ", scope=" + scope
				+ ", snmpCheckInfo=" + snmpCheckInfo + ", sqlCheckInfo=" + sqlCheckInfo + ", winEventCheckInfo="
				+ winEventCheckInfo + ", logfileCheckInfo=" + logfileCheckInfo + ", rpaLogfileCheckInfo=" + rpaLogFileCheckInfo + 
				", pluginCheckInfo=" + pluginCheckInfo
				+ ", stringValueInfo=" + stringValueInfo + ", logFormatId=" + logFormatId + ", notifyRelationList="
				+ notifyRelationList + ", regDate=" + regDate + ", regUser=" + regUser + ", updateDate="
				+ updateDate + ", updateUser=" + updateUser + "]";
	}

	
}
