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

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;
import com.clustercontrol.rest.endpoint.repository.dto.enumtype.NodeConfigRunIntervalEnum;

public class NodeConfigSettingInfoResponse {

	private String settingId;
	private String settingName;
	private String description;
	private String facilityId;
	@RestPartiallyTransrateTarget
	private String scope;
	@RestBeanConvertEnum
	private NodeConfigRunIntervalEnum runInterval;
	private String calendarId;
	private Boolean validFlg;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	private String regUser;
	private String updateUser;
	private List<NotifyRelationInfoResponse> notifyRelationList = new ArrayList<>();
	private List<NodeConfigSettingItemInfoResponse> nodeConfigSettingItemList = new ArrayList<>();
	private List<NodeConfigCustomInfoResponse> nodeConfigCustomList = new ArrayList<>();

	private String ownerRoleId;

	public NodeConfigSettingInfoResponse() {
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

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public List<NotifyRelationInfoResponse> getNotifyRelationList() {
		return notifyRelationList;
	}

	public void setNotifyRelationList(List<NotifyRelationInfoResponse> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}

	public List<NodeConfigSettingItemInfoResponse> getNodeConfigSettingItemList() {
		return nodeConfigSettingItemList;
	}

	public void setNodeConfigSettingItemList(List<NodeConfigSettingItemInfoResponse> nodeConfigSettingItemList) {
		this.nodeConfigSettingItemList = nodeConfigSettingItemList;
	}

	public List<NodeConfigCustomInfoResponse> getNodeConfigCustomList() {
		return nodeConfigCustomList;
	}

	public void setNodeConfigCustomList(List<NodeConfigCustomInfoResponse> nodeConfigCustomList) {
		this.nodeConfigCustomList = nodeConfigCustomList;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
}
