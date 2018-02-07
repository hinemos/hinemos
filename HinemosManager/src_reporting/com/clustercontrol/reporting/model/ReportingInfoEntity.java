/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;


/**
 * The persistent class for the cc_reporting_info database table.
 * 
 */
@Entity
@Table(name="cc_reporting_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType="REPORTING",
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="report_schedule_id", insertable=false, updatable=false))
public class ReportingInfoEntity extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	private String reportScheduleId;
	private String description;
	private String facilityId;
	private String calendarId;
	private Integer outputPeriodType;
	private Integer outputPeriodBefore;
	private Integer outputPeriodFor;
	private String templateSetId;
	private String reportTitle;
	private Boolean logoValidFlg;
	private String logoFilename;
	private Boolean pageValidFlg;
	private Integer outputType;
	private Integer scheduleType;
	private Integer day;
	private Integer week;
	private Integer hour;
	private Integer minute;
	private String notifyGroupId;
	private Boolean validFlg;
	private Long regDate;
	private Long updateDate;
	private String regUser;
	private String updateUser;

	@Deprecated
	public ReportingInfoEntity() {
	}

	public ReportingInfoEntity(String reportScheduleId) {
		this.setReportScheduleId(reportScheduleId);
		this.setObjectId(this.getReportScheduleId());
	}


	@Id
	@Column(name="report_schedule_id")
	public String getReportScheduleId() {
		return this.reportScheduleId;
	}

	public void setReportScheduleId(String reportScheduleId) {
		this.reportScheduleId = reportScheduleId;
	}

	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}
	
	@Column(name="output_period_type")
	public Integer getOutputPeriodType() {
		return this.outputPeriodType;
	}

	public void setOutputPeriodType(Integer outputPeriodType) {
		this.outputPeriodType = outputPeriodType;
	}
	
	@Column(name="output_period_before")
	public Integer getOutputPeriodBefore() {
		return this.outputPeriodBefore;
	}

	public void setOutputPeriodBefore(Integer outputPeriodBefore) {
		this.outputPeriodBefore = outputPeriodBefore;
	}

	@Column(name="output_period_for")
	public Integer getOutputPeriodFor() {
		return this.outputPeriodFor;
	}

	public void setOutputPeriodFor(Integer outputPeriodFor) {
		this.outputPeriodFor = outputPeriodFor;
	}
	
	@Column(name="template_set_id")
	public String getTemplateSetId() {
		return this.templateSetId;
	}
	
	public void setTemplateSetId(String templateSetId) {
		this.templateSetId = templateSetId;
	}
	
	@Column(name="report_title")
	public String getReportTitle() {
		return this.reportTitle;
	}

	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}
	
	@Column(name="logo_valid_flg")
	public Boolean getLogoValidFlg() {
		return this.logoValidFlg;
	}

	public void setLogoValidFlg(Boolean logoValidFlg) {
		this.logoValidFlg = logoValidFlg;
	}
	
	@Column(name="logo_filename")
	public String getLogoFilename() {
		return this.logoFilename;
	}

	public void setLogoFilename(String logoFilename) {
		this.logoFilename = logoFilename;
	}
	
	@Column(name="page_valid_flg")
	public Boolean getPageValidFlg() {
		return this.pageValidFlg;
	}

	public void setPageValidFlg(Boolean pageValidFlg) {
		this.pageValidFlg = pageValidFlg;
	}
	
	@Column(name="output_type")
	public Integer getOutputType() {
		return this.outputType;
	}

	public void setOutputType(Integer outputType) {
		this.outputType = outputType;
	}
	
	@Column(name="schedule_type")
	public Integer getScheduleType() {
		return this.scheduleType;
	}

	public void setScheduleType(Integer scheduleType) {
		this.scheduleType = scheduleType;
	}
	
	@Column(name="day")
	public Integer getDay() {
		return this.day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}
	
	@Column(name="week")
	public Integer getWeek() {
		return this.week;
	}

	public void setWeek(Integer week) {
		this.week = week;
	}
	
	@Column(name="hour")
	public Integer getHour() {
		return this.hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}
	
	@Column(name="minute")
	public Integer getMinute() {
		return this.minute;
	}

	public void setMinute(Integer minute) {
		this.minute = minute;
	}
	
	@Column(name="notify_group_id")
	public String getNotifyGroupId() {
		return this.notifyGroupId;
	}

	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}

	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return this.validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
}