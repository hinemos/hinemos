/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;
import com.clustercontrol.rest.endpoint.reporting.dto.enumtype.ReportOutputPeriodTypeEnum;
import com.clustercontrol.rest.endpoint.reporting.dto.enumtype.ReportOutputTypeEnum;

public class ModifyReportingScheduleRequest implements RequestDto {
	
	public ModifyReportingScheduleRequest (){}
	
	private String description;
	private String facilityId;
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

	/** スケジュール */
	private ReportingScheduleInfoRequest schedule;

	/**通知*/
	private List<NotifyRelationInfoRequest> notifyRelationList;

	/**
	 * 
	 * @return
	 */
	public List<NotifyRelationInfoRequest> getNotifyRelationList() {
		return notifyRelationList;
	}

	/**
	 * 
	 * @param notifyRelationList
	 */
	public void setNotifyRelationList(List<NotifyRelationInfoRequest> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
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

	public ReportingScheduleInfoRequest getSchedule() {
		return schedule;
	}

	public void setSchedule(ReportingScheduleInfoRequest schedule) {
		this.schedule = schedule;
	}

	@Override
	public String toString() {
		return "ModifyReportingRequest [description=" + description
				+ ", facilityId=" + facilityId
				+ ", calendarId=" + calendarId + ", outputPeriodType=" + outputPeriodType
				+ ", outputPeriodBefore=" + outputPeriodBefore + ", outputPeriodFor=" + outputPeriodFor
				+ ", templateSetId=" + templateSetId + ", reportTitle=" + reportTitle + ", logoValidFlg=" + logoValidFlg
				+ ", logoFilename=" + logoFilename + ", pageValidFlg=" + pageValidFlg + ", outputType=" + outputType
				+ ", validFlg=" + validFlg
				+ ", schedule=" + schedule + ", notifyRelationList=" + notifyRelationList + "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
