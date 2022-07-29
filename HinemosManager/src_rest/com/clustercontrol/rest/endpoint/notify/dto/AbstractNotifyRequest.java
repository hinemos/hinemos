/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.RenotifyTypeEnum;
import com.clustercontrol.util.MessageConstant;

public abstract class AbstractNotifyRequest implements RequestDto {

	@RestItemName(value = MessageConstant.DESCRIPTION)
	protected String description;

	@RestItemName(value = MessageConstant.NOTIFY_INITIAL)
	protected Integer initialCount;

	protected Boolean notFirstNotify;

	@RestBeanConvertEnum
	protected NotifyTypeEnum notifyType;

	@RestItemName(value = MessageConstant.SUPPRESS_BY_TIME_INTERVAL)
	protected Integer renotifyPeriod;

	@RestBeanConvertEnum
	protected RenotifyTypeEnum renotifyType;

	protected Boolean validFlg;

	@RestItemName(value = MessageConstant.CALENDAR_ID)
	protected String calendarId;

	public AbstractNotifyRequest() {
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getInitialCount() {
		return initialCount;
	}

	public void setInitialCount(Integer initialCount) {
		this.initialCount = initialCount;
	}

	public Boolean getNotFirstNotify() {
		return notFirstNotify;
	}

	public void setNotFirstNotify(Boolean notFirstNotify) {
		this.notFirstNotify = notFirstNotify;
	}

	public NotifyTypeEnum getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(NotifyTypeEnum notifyType) {
		this.notifyType = notifyType;
	}

	public Integer getRenotifyPeriod() {
		return renotifyPeriod;
	}

	public void setRenotifyPeriod(Integer renotifyPeriod) {
		this.renotifyPeriod = renotifyPeriod;
	}

	public RenotifyTypeEnum getRenotifyType() {
		return renotifyType;
	}

	public void setRenotifyType(RenotifyTypeEnum renotifyType) {
		this.renotifyType = renotifyType;
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

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
