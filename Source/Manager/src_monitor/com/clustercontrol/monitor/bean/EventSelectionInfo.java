/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

/**
 * クライアント側で選択されているイベント履歴を表すための情報です。
 */
public class EventSelectionInfo {

	private Long outputDate;
	private String pluginId;
	private String monitorId;
	private String monitorDetailId;
	private String facilityId;

	public EventSelectionInfo() {
	}

	public Long getOutputDate() {
		return outputDate;
	}

	public void setOutputDate(Long outputDate) {
		this.outputDate = outputDate;
	}

	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

}
