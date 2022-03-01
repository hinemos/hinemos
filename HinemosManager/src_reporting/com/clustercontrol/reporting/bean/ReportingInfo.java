/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.bean;

import java.io.Serializable;
import java.util.List;

import com.clustercontrol.commons.bean.Schedule;
import com.clustercontrol.notify.model.NotifyRelationInfo;

/**
 * レポーティング スケジュール情報のデータクラスです。
 * 
 * @version 5.0.a
 * @since 4.1.2
 *
 */
public class ReportingInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	private String reportScheduleId = null;
	private String description = null;
	private String ownerRoleId = null;
	private String facilityId = null;
	private String scope = null;
	private String calendarId = null;
	private Integer outputPeriodType = Integer.valueOf(0);
	private Integer outputPeriodBefore = null;
	private Integer outputPeriodFor = null;
	private String templateSetId = null;;
	private String reportTitle = null;
	private Boolean logoValidFlg = null;
	private String logoFilename = null;
	private Boolean pageValidFlg = null;
	private Integer outputType = Integer.valueOf(0);
	private String notifyGroupId = null;;
	private Boolean validFlg = null;
	private Long regDate = Long.valueOf(0);
	private Long updateDate = Long.valueOf(0);
	private String regUser = null;
	private String updateUser = null;

	/** スケジュール */
	private Schedule schedule;

	/**通知*/
	private List<NotifyRelationInfo> notifyRelationList = null;

	public ReportingInfo() {
	}

	/**
	 * 
	 * @return
	 */
	public List<NotifyRelationInfo> getNotifyRelationList() {
		return notifyRelationList;
	}

	/**
	 * 
	 * @param notifyRelationList
	 */
	public void setNotifyRelationList(List<NotifyRelationInfo> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}

	public String getReportScheduleId() {
		return reportScheduleId;
	}

	public void setReportScheduleId(String reportScheduleId) {
		this.reportScheduleId = reportScheduleId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getScope() {
		return scope;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public Integer getOutputPeriodType() {
		return outputPeriodType;
	}

	public void setOutputPeriodType(Integer outputPeriodType) {
		this.outputPeriodType = outputPeriodType;
	}

	public Integer getOutputPeriodBefore() {
		return outputPeriodBefore;
	}

	public void setOutputPeriodBefore(Integer outputPeriodBefore) {
		this.outputPeriodBefore = outputPeriodBefore;
	}

	public Integer getOutputPeriodFor() {
		return outputPeriodFor;
	}

	public void setOutputPeriodFor(Integer outputPeriodFor) {
		this.outputPeriodFor = outputPeriodFor;
	}

	public String getTemplateSetId() {
		return templateSetId;
	}

	public void setTemplateSetId(String templateSetId) {
		this.templateSetId = templateSetId;
	}

	public String getReportTitle() {
		return reportTitle;
	}

	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}

	public Boolean getLogoValidFlg() {
		return logoValidFlg;
	}

	public void setLogoValidFlg(Boolean logoValidFlg) {
		this.logoValidFlg = logoValidFlg;
	}

	public String getLogoFilename() {
		return logoFilename;
	}

	public void setLogoFilename(String logoFilename) {
		this.logoFilename = logoFilename;
	}

	public Boolean getPageValidFlg() {
		return pageValidFlg;
	}

	public void setPageValidFlg(Boolean pageValidFlg) {
		this.pageValidFlg = pageValidFlg;
	}

	public Integer getOutputType() {
		return outputType;
	}

	public void setOutputType(Integer outputType) {
		this.outputType = outputType;
	}

	public String getNotifyGroupId() {
		return notifyGroupId;
	}

	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	public Long getRegDate() {
		return regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
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

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

}
