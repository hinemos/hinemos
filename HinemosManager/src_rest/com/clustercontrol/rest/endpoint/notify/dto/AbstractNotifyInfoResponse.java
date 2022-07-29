/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.RenotifyTypeEnum;

public abstract class AbstractNotifyInfoResponse {
	protected String ownerRoleId;
	protected String notifyId;
	protected String description;
	protected Integer initialCount;
	protected Boolean notFirstNotify;
	@RestBeanConvertEnum
	protected NotifyTypeEnum notifyType;
	@RestBeanConvertDatetime
	protected String regDate;
	protected String regUser;
	protected Integer renotifyPeriod;
	@RestBeanConvertEnum
	protected RenotifyTypeEnum renotifyType;
	@RestBeanConvertDatetime
	protected String updateDate;
	protected String updateUser;
	protected Boolean validFlg;
	protected String calendarId;

	public AbstractNotifyInfoResponse() {
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getNotifyId() {
		return notifyId;
	}

	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
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
}