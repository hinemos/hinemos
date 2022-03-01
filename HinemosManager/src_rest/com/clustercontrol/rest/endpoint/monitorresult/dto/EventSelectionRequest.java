/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.monitor.bean.EventSelectionInfo;
import com.clustercontrol.repository.bean.FacilityIdConstant;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;

/**
 * 選択されているイベントの情報を表します。
 */
@RestBeanConvertAssertion(to = EventSelectionInfo.class)
public class EventSelectionRequest implements RequestDto {
	
	/** 受信日時 */
	@RestBeanConvertDatetime
	private String outputDate;
	/** プラグインID */
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64)
	private String pluginId;
	/** 監視項目ID */
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64)
	private String monitorId;
	/** 監視詳細 */
	@RestValidateString(notNull = true, minLen = 0, maxLen = 1024)
	private String monitorDetailId;
	/** ファシリティID */
	@RestValidateString(notNull = true, minLen = 1, maxLen = FacilityIdConstant.MAX_LEN)
	private String facilityId;

	public EventSelectionRequest() {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// NOP
	}

	public String getOutputDate() {
		return outputDate;
	}

	public void setOutputDate(String outputDate) {
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
