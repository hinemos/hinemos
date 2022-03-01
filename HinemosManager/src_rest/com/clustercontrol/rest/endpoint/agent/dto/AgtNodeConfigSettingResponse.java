/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

import com.clustercontrol.repository.bean.NodeConfigSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = NodeConfigSetting.class)
public class AgtNodeConfigSettingResponse {

	// ---- from NodeConfigSetting
	private String settingId;
	private String settingName;
	private String facilityId;
	private Integer runInterval;
	private AgtCalendarInfoResponse calendar;
	private List<String> nodeConfigSettingItemList;
	private List<AgtNodeConfigCustomInfoResponse> nodeConfigSettingCustomList;
	private String referenceTime;
	private Long loadDistributionRange;

	public AgtNodeConfigSettingResponse() {
	}

	// ---- accessors

	public String getSettingId() {
		return settingId;
	}

	public void setSettingId(String settingId) {
		this.settingId = settingId;
	}

	public String getSettingName() {
		return settingName;
	}

	public void setSettingName(String settingName) {
		this.settingName = settingName;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public Integer getRunInterval() {
		return runInterval;
	}

	public void setRunInterval(Integer runInterval) {
		this.runInterval = runInterval;
	}

	public AgtCalendarInfoResponse getCalendar() {
		return calendar;
	}

	public void setCalendar(AgtCalendarInfoResponse calendar) {
		this.calendar = calendar;
	}

	public List<String> getNodeConfigSettingItemList() {
		return nodeConfigSettingItemList;
	}

	public void setNodeConfigSettingItemList(List<String> nodeConfigSettingItemList) {
		this.nodeConfigSettingItemList = nodeConfigSettingItemList;
	}

	public List<AgtNodeConfigCustomInfoResponse> getNodeConfigSettingCustomList() {
		return nodeConfigSettingCustomList;
	}

	public void setNodeConfigSettingCustomList(List<AgtNodeConfigCustomInfoResponse> nodeConfigSettingCustomList) {
		this.nodeConfigSettingCustomList = nodeConfigSettingCustomList;
	}

	public String getReferenceTime() {
		return referenceTime;
	}

	public void setReferenceTime(String referenceTime) {
		this.referenceTime = referenceTime;
	}

	public Long getLoadDistributionRange() {
		return loadDistributionRange;
	}

	public void setLoadDistributionRange(Long loadDistributionRange) {
		this.loadDistributionRange = loadDistributionRange;
	}

}
