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

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.RunIntervalEnum;

public abstract class AbstractMonitorRequest implements RequestDto {

	public AbstractMonitorRequest() {

	}

	protected String application;
	protected String description;
	protected Boolean monitorFlg;
	@RestBeanConvertEnum
	protected RunIntervalEnum runInterval;
	protected String calendarId;
	protected String facilityId;
	protected List<NotifyRelationInfoRequest> notifyRelationList = new ArrayList<>();

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
	public String getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public List<NotifyRelationInfoRequest> getNotifyRelationList() {
		return notifyRelationList;
	}
	public void setNotifyRelationList(List<NotifyRelationInfoRequest> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
