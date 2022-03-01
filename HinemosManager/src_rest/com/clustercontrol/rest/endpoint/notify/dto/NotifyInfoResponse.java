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

public class NotifyInfoResponse {
	private String ownerRoleId;
	private String notifyId;
	private String description;
	private Integer initialCount;
	private Boolean notFirstNotify;
	@RestBeanConvertEnum
	private NotifyTypeEnum notifyType;
	@RestBeanConvertDatetime
	private String regDate;
	private String regUser;
	private Integer renotifyPeriod;
	@RestBeanConvertEnum
	private RenotifyTypeEnum renotifyType;
	@RestBeanConvertDatetime
	private String updateDate;
	private String updateUser;
	private Boolean validFlg;
	private String calendarId;

	public NotifyInfoResponse() {
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
