/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;
import com.clustercontrol.rest.endpoint.reporting.dto.enumtype.ReportOutputPeriodTypeEnum;
import com.clustercontrol.rest.endpoint.reporting.dto.enumtype.ReportOutputTypeEnum;

public class ReportingScheduleResponse {
	
	public ReportingScheduleResponse() {
	}

	private String reportScheduleId;
	private String description;
	private String ownerRoleId;
	private String facilityId;
	@RestPartiallyTransrateTarget
	private String scope;
	private String calendarId;
	@RestBeanConvertEnum
	private ReportOutputPeriodTypeEnum outputPeriodType;
	private Integer outputPeriodBefore;
	private Integer outputPeriodFor;
	private String templateSetId;
	private String reportTitle;
	private Boolean logoValidFlg;
	private String logoFilename;
	private Boolean pageValidFlg;
	@RestBeanConvertEnum
	private ReportOutputTypeEnum outputType;
	private Boolean validFlg;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	private String regUser;
	private String updateUser;

	/** スケジュール */
	private ReportingScheduleInfoResponse schedule;

	/**通知*/
	private List<NotifyRelationInfoResponse> notifyRelationList;

	/**
	 * 
	 * @return
	 */
	public List<NotifyRelationInfoResponse> getNotifyRelationList() {
		return notifyRelationList;
	}

	/**
	 * 
	 * @param notifyRelationList
	 */
	public void setNotifyRelationList(List<NotifyRelationInfoResponse> notifyRelationList) {
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

	public ReportOutputPeriodTypeEnum getOutputPeriodType() {
		return outputPeriodType;
	}

	public void setOutputPeriodType(ReportOutputPeriodTypeEnum outputPeriodType) {
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

	public ReportOutputTypeEnum getOutputType() {
		return outputType;
	}

	public void setOutputType(ReportOutputTypeEnum outputType) {
		this.outputType = outputType;
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

	public ReportingScheduleInfoResponse getSchedule() {
		return schedule;
	}

	public void setSchedule(ReportingScheduleInfoResponse schedule) {
		this.schedule = schedule;
	}

	@Override
	public String toString() {
		return "ReportingScheduleResponse [reportScheduleId=" + reportScheduleId + ", description=" + description
				+ ", ownerRoleId=" + ownerRoleId + ", facilityId=" + facilityId + ", scope=" + scope
				+ ", calendarId=" + calendarId + ", outputPeriodType=" + outputPeriodType
				+ ", outputPeriodBefore=" + outputPeriodBefore + ", outputPeriodFor=" + outputPeriodFor
				+ ", templateSetId=" + templateSetId + ", reportTitle=" + reportTitle + ", logoValidFlg=" + logoValidFlg
				+ ", logoFilename=" + logoFilename + ", pageValidFlg=" + pageValidFlg + ", outputType=" + outputType
				+ ", validFlg=" + validFlg + ", regDate = " + regDate
				+ ", updateDate=" + updateDate + ", regUser=" + regUser + ", updateUser=" + updateUser
				+ ", schedule=" + schedule + ", notifyRelationList=" + notifyRelationList + "]";
	}

}
