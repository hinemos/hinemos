/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.NodeConfigRunIntervalEnum;

public class AddNodeConfigSettingInfoRequest implements RequestDto {

	private String settingId;
	private String settingName;
	private String description;
	private String facilityId;
	private String scope;
	@RestBeanConvertEnum
	private NodeConfigRunIntervalEnum runInterval;
	private String calendarId;
	private Boolean validFlg;
	private List<NotifyRelationInfoRequest> notifyRelationList = new ArrayList<>();
	private List<NodeConfigSettingItemInfoRequest> nodeConfigSettingItemList = new ArrayList<>();
	private List<NodeConfigCustomInfoRequest> nodeConfigCustomList = new ArrayList<>();

	private String ownerRoleId;

	public AddNodeConfigSettingInfoRequest() {
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public NodeConfigRunIntervalEnum getRunInterval() {
		return runInterval;
	}

	public void setRunInterval(NodeConfigRunIntervalEnum runInterval) {
		this.runInterval = runInterval;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	public List<NotifyRelationInfoRequest> getNotifyRelationList() {
		return notifyRelationList;
	}

	public void setNotifyRelationList(List<NotifyRelationInfoRequest> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}

	public List<NodeConfigSettingItemInfoRequest> getNodeConfigSettingItemList() {
		return nodeConfigSettingItemList;
	}

	public void setNodeConfigSettingItemList(List<NodeConfigSettingItemInfoRequest> nodeConfigSettingItemList) {
		this.nodeConfigSettingItemList = nodeConfigSettingItemList;
	}

	public List<NodeConfigCustomInfoRequest> getNodeConfigCustomList() {
		return nodeConfigCustomList;
	}

	public void setNodeConfigCustomList(List<NodeConfigCustomInfoRequest> nodeConfigCustomList) {
		this.nodeConfigCustomList = nodeConfigCustomList;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
