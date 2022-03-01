/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.maintenance.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;
import com.clustercontrol.rest.endpoint.maintenance.dto.enumtype.MaintenanceType;
import com.clustercontrol.util.MessageConstant;

public class AddMaintenanceRequest implements RequestDto {
	@RestItemName(value = MessageConstant.MAINTENANCE_ID)
	@RestValidateString(notNull = true, minLen = 1, type = CheckType.ID)
	private String maintenanceId;
	@RestItemName(value = MessageConstant.APPLICATION)
	private String application;
	@RestItemName(value = MessageConstant.MAINTENANCE_RETENTION_PERIOD)
	private Integer dataRetentionPeriod;
	@RestItemName(value = MessageConstant.DESCRIPTION)
	private String description;
	@RestItemName(value = MessageConstant.VALID_FLG)
	@RestValidateObject(notNull = true)
	private Boolean validFlg;
	@RestItemName(value = MessageConstant.CALENDAR_ID)
	private String calendarId;
	@RestItemName(value = MessageConstant.SCHEDULE)
	@RestValidateObject(notNull = true)
	private MaintenanceScheduleRequest schedule;
	@RestItemName(value = MessageConstant.NOTIFY_ID)
	private List<NotifyRelationInfoRequest> notifyId;
	@RestItemName(value = MessageConstant.MAINTENANCE_TYPE)
	@RestBeanConvertEnum
	private MaintenanceType typeId;
	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(notNull = true)
	private String ownerRoleId;
	
	public AddMaintenanceRequest() {
	} 
	
	@Override
	public void correlationCheck() throws InvalidSetting {
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
	public MaintenanceScheduleRequest getSchedule() {
		return schedule;
	}
	public void setSchedule(MaintenanceScheduleRequest schedule) {
		this.schedule = schedule;
	}
	public List<NotifyRelationInfoRequest> getNotifyId() {
		return notifyId;
	}
	public void setNotifyId(List<NotifyRelationInfoRequest> notifyId) {
		this.notifyId = notifyId;
	}
	public MaintenanceType getTypeId() {
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
		return "AddMaintenanceRequest [maintenanceId=" + maintenanceId + ", application=" + application
				+ ", dataRetentionPeriod=" + dataRetentionPeriod + ", description=" + description + ", validFlg=" + validFlg
				+ ", calendarId=" + calendarId + ", typeId=" + typeId + "]";
	}
}
