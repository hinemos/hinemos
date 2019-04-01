/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.repository.model.NodeConfigCustomInfo;

/**
 * 対象構成情報の格納クラス<BR />
 * 
 * @version 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeConfigSetting implements Serializable {
	private static final long serialVersionUID = -4324117941296918253L;

	// 対象構成情報ID
	private String settingId = "";
	// 対象構成情報名
	private String settingName = "";
	// 対象のノードID
	private String facilityId = "";
	// 間隔
	private Integer runInterval = 0;
	// カレンダ情報
	private CalendarInfo calendar;
	// 収集対象
	private List<String> nodeConfigSettingItemList = new ArrayList<>();
	// ユーザ任意情報
	private List<NodeConfigCustomInfo> nodeConfigSettingCustomList = new ArrayList<>();

	// 以下、Manager→Agent送信用の項目(Hinemosプロパティから取得).
	// 収集基準時刻(HH:mm 24時間表記).
	private String referenceTime = HinemosPropertyCommon.repository_node_config_collect_reference_time.getStringValue();
	// 収集基準時刻範囲(分)
	private Long loadDistributionRange = HinemosPropertyCommon.repository_node_config_collect_load_distribution_range
			.getNumericValue();

	public NodeConfigSetting() {

	}

	public NodeConfigSetting(String settingId, String settingName, String facilityId, Integer runInterval,
			CalendarInfo calendar, List<String> nodeConfigSettingItemList,
			List<NodeConfigCustomInfo> nodeConfigSettingCustomList, String referenceTime, Long loadDistributionRange) {
		this.settingId = settingId;
		this.settingName = settingName;
		this.facilityId = facilityId;
		this.runInterval = runInterval;
		this.calendar = calendar;
		this.nodeConfigSettingItemList = nodeConfigSettingItemList;
		this.nodeConfigSettingCustomList = nodeConfigSettingCustomList;
		this.referenceTime = referenceTime;
		this.loadDistributionRange = loadDistributionRange;
	}

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

	public CalendarInfo getCalendar() {
		return calendar;
	}

	public void setCalendar(CalendarInfo calendar) {
		this.calendar = calendar;
	}

	public List<String> getNodeConfigSettingItemList() {
		return nodeConfigSettingItemList;
	}

	public void setNodeConfigSettingItemList(List<String> nodeConfigSettingItemList) {
		this.nodeConfigSettingItemList = nodeConfigSettingItemList;
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

	public List<NodeConfigCustomInfo> getNodeConfigSettingCustomList() {
		return nodeConfigSettingCustomList;
	}

	public void setNodeConfigSettingCustomList(List<NodeConfigCustomInfo> nodeConfigSettingCustomList) {
		this.nodeConfigSettingCustomList = nodeConfigSettingCustomList;
	}
}
