/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.reporting.dto.enumtype.ReportOutputPeriodTypeEnum;

public class CreateReportingFileRequest implements RequestDto {
	
	public CreateReportingFileRequest (){}
	
	private String reportScheduleId;
	@RestBeanConvertEnum
	private ReportOutputPeriodTypeEnum outputPeriodType;
	private Integer outputPeriodBefore;
	private Integer outputPeriodFor;
	private Boolean notifyFlg;

	public String getReportScheduleId() {
		return reportScheduleId;
	}

	public void setReportScheduleId(String reportScheduleId) {
		this.reportScheduleId = reportScheduleId;
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

	public Boolean getNotifyFlg() {
		return notifyFlg;
	}

	public void setNotifyFlg(Boolean notifyFlg) {
		this.notifyFlg = notifyFlg;
	}

	@Override
	public String toString() {
		return "CreateReportingFileRequest [reportScheduleId=" + reportScheduleId + ", outputPeriodType=" + outputPeriodType
				+ ", outputPeriodBefore=" + outputPeriodBefore + ", outputPeriodFor=" + outputPeriodFor + ", notifyFlg=" + notifyFlg + "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
