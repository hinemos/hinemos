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

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.endpoint.calendar.dto.CalendarInfoResponse;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.MonitorTypeEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PriorityEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.RunIntervalEnum;

public class ModifyMonitorInfoResponse {

	private String monitorId;
	@RestBeanConvertEnum
	private MonitorTypeEnum monitorType;
	private String monitorTypeId;
	private String application;
	private String description;
	private Boolean monitorFlg;
	@RestBeanConvertEnum
	private RunIntervalEnum runInterval;
	private CalendarInfoResponse calendar;
	private String facilityId;
	private String scope;
	private List<MonitorTruthValueInfoResponse> truthValueInfo = new ArrayList<>();
	private List<NotifyRelationInfoResponse> notifyRelationList = new ArrayList<>();
	private String ownerRoleId;
	private Integer delayTime;
	@RestBeanConvertEnum
	private PriorityEnum failurePriority;
	private String triggerType;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	private String regUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	private String updateUser;

	public ModifyMonitorInfoResponse() {
	}

	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	public MonitorTypeEnum getMonitorType() {
		return monitorType;
	}
	public void setMonitorType(MonitorTypeEnum monitorType) {
		this.monitorType = monitorType;
	}
	public String getMonitorTypeId() {
		return monitorTypeId;
	}
	public void setMonitorTypeId(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getMonitorFlg() {
		return monitorFlg;
	}
	public void setMonitorFlg(Boolean monitorFlg) {
		this.monitorFlg = monitorFlg;
	}
	public RunIntervalEnum getRunInterval() {
		return runInterval;
	}
	public void setRunInterval(RunIntervalEnum runInterval) {
		this.runInterval = runInterval;
	}
	public CalendarInfoResponse getCalendar() {
		return calendar;
	}
	public void setCalendar(CalendarInfoResponse calendar) {
		this.calendar = calendar;
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

	public List<MonitorTruthValueInfoResponse> getTruthValueInfo() {
		return truthValueInfo;
	}
	public void setTruthValueInfo(List<MonitorTruthValueInfoResponse> truthValueInfo) {
		this.truthValueInfo = truthValueInfo;
	}
	public List<NotifyRelationInfoResponse> getNotifyRelationList() {
		return notifyRelationList;
	}
	public void setNotifyRelationList(List<NotifyRelationInfoResponse> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	public Integer getDelayTime() {
		return delayTime;
	}
	public void setDelayTime(Integer delayTime) {
		this.delayTime = delayTime;
	}
	public PriorityEnum getFailurePriority() {
		return failurePriority;
	}
	public void setFailurePriority(PriorityEnum failurePriority) {
		this.failurePriority = failurePriority;
	}
	public String getTriggerType() {
		return triggerType;
	}
	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
	}
	public String getRegDate() {
		return regDate;
	}
	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}
	public String getRegUser() {
		return regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public String getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	@Override
	public String toString() {
		return "ModifyMonitorInfoResponse [monitorId=" + monitorId + ", monitorType=" + monitorType + ", monitorTypeId="
				+ monitorTypeId + ", application=" + application + ", description=" + description + ", monitorFlg="
				+ monitorFlg + ", runInterval=" + runInterval + ", calendar=" + calendar + ", facilityId=" + facilityId
				+ ", scope=" + scope + ", truthValueInfo=" + truthValueInfo + ", notifyRelationList="
				+ notifyRelationList + ", ownerRoleId=" + ownerRoleId + ", delayTime=" + delayTime
				+ ", failurePriority=" + failurePriority + ", triggerType=" + triggerType + ", regDate=" + regDate
				+ ", regUser=" + regUser + ", updateDate=" + updateDate + ", updateUser=" + updateUser + "]";
	}
}
