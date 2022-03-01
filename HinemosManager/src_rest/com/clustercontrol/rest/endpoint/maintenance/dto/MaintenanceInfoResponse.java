/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.maintenance.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;
import com.clustercontrol.rest.endpoint.maintenance.dto.enumtype.MaintenanceType;

public class MaintenanceInfoResponse {
	private String maintenanceId;
	private String application;
	private Integer dataRetentionPeriod;
	private String description;
	@RestBeanConvertDatetime
	private String regDate;
	private String regUser;
	@RestBeanConvertDatetime
	private String updateDate;
	private String updateUser;
	private Boolean validFlg;
	private String calendarId;
	private MaintenanceScheduleResponse schedule;
	private List<NotifyRelationInfoResponse> notifyId;
	private MaintenanceTypeInfoResponse maintenanceTypeMstEntity;
	@RestBeanConvertEnum
	private MaintenanceType typeId;
	private String ownerRoleId;
	
	public MaintenanceInfoResponse() {
	}

	public String getMaintenanceId() {
		return maintenanceId;
	}
	public void setMaintenanceId(String maintenanceId) {
		this.maintenanceId = maintenanceId;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public Integer getDataRetentionPeriod() {
		return dataRetentionPeriod;
	}
	public void setDataRetentionPeriod(Integer dataRetentionPeriod) {
		this.dataRetentionPeriod = dataRetentionPeriod;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}
	public String getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
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
	public MaintenanceScheduleResponse getSchedule() {
		return schedule;
	}
	public void setSchedule(MaintenanceScheduleResponse schedule) {
		this.schedule = schedule;
	}
	public List<NotifyRelationInfoResponse> getNotifyId() {
		return notifyId;
	}
	public void setNotifyId(List<NotifyRelationInfoResponse> notifyId) {
		this.notifyId = notifyId;
	}
	public MaintenanceTypeInfoResponse getMaintenanceTypeMstEntity() {
		return maintenanceTypeMstEntity;
	}
	public void setMaintenanceTypeMstEntity(MaintenanceTypeInfoResponse maintenanceTypeMstEntity) {
		this.maintenanceTypeMstEntity = maintenanceTypeMstEntity;
	}
	public MaintenanceType getTypeId() {
		if (typeId == null && maintenanceTypeMstEntity != null)
			typeId = maintenanceTypeMstEntity.getTypeId();
		return typeId;
	}
	public void setTypeId(MaintenanceType typeId) {
		this.typeId = typeId;
	}
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	@Override
	public String toString() {
		return "MaintenanceInfoResponse [maintenanceId=" + maintenanceId + ", application=" + application
				+ ", dataRetentionPeriod=" + dataRetentionPeriod + ", description=" + description + ", validFlg=" + validFlg
				+ ", calendarId=" + calendarId + ", typeId=" + typeId + "]";
	}
}
