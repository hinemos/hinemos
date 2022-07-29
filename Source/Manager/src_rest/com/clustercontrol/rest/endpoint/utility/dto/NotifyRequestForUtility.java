/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.CloudNotifyDetailInfoRequest;
import com.clustercontrol.rest.endpoint.notify.dto.CommandNotifyDetailInfoRequest;
import com.clustercontrol.rest.endpoint.notify.dto.EventNotifyDetailInfoRequest;
import com.clustercontrol.rest.endpoint.notify.dto.InfraNotifyDetailInfoRequest;
import com.clustercontrol.rest.endpoint.notify.dto.JobNotifyDetailInfoRequest;
import com.clustercontrol.rest.endpoint.notify.dto.LogEscalateNotifyDetailInfoRequest;
import com.clustercontrol.rest.endpoint.notify.dto.MailNotifyDetailInfoRequest;
import com.clustercontrol.rest.endpoint.notify.dto.MessageNotifyDetailInfoRequest;
import com.clustercontrol.rest.endpoint.notify.dto.RestNotifyDetailInfoRequest;
import com.clustercontrol.rest.endpoint.notify.dto.StatusNotifyDetailInfoRequest;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyTypeEnum;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.RenotifyTypeEnum;

public class NotifyRequestForUtility implements RequestDto{
	private String notifyId;
	private String description;
	private Integer initialCount;
	private Boolean notFirstNotify;
	private NotifyTypeEnum notifyType;
	private String regDate;
	private String regUser;
	private Integer renotifyPeriod;
	private RenotifyTypeEnum renotifyType;
	private String updateDate;
	private String updateUser;
	private Boolean validFlg;
	private String calendarId;
	private String ownerRoleId;

	private CommandNotifyDetailInfoRequest notifyCommandInfo;
	private EventNotifyDetailInfoRequest notifyEventInfo;
	private JobNotifyDetailInfoRequest notifyJobInfo;
	private LogEscalateNotifyDetailInfoRequest notifyLogEscalateInfo;
	private MailNotifyDetailInfoRequest notifyMailInfo;
	private StatusNotifyDetailInfoRequest notifyStatusInfo;
	private InfraNotifyDetailInfoRequest notifyInfraInfo;
	private RestNotifyDetailInfoRequest notifyRestInfo;
	private MessageNotifyDetailInfoRequest notifyMessageInfo;
	private CloudNotifyDetailInfoRequest notifyCloudInfo;

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

	public NotifyTypeEnum getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(NotifyTypeEnum notifyType) {
		this.notifyType = notifyType;
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

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
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

	public RenotifyTypeEnum getRenotifyType() {
		return renotifyType;
	}

	public void setRenotifyType(RenotifyTypeEnum renotifyType) {
		this.renotifyType = renotifyType;
	}

	public Integer getRenotifyPeriod() {
		return renotifyPeriod;
	}

	public void setRenotifyPeriod(Integer renotifyPeriod) {
		this.renotifyPeriod = renotifyPeriod;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public StatusNotifyDetailInfoRequest getNotifyStatusInfo() {
		return notifyStatusInfo;
	}

	public void setNotifyStatusInfo(StatusNotifyDetailInfoRequest notifyStatusInfo) {
		this.notifyStatusInfo = notifyStatusInfo;
	}

	public EventNotifyDetailInfoRequest getNotifyEventInfo() {
		return notifyEventInfo;
	}

	public void setNotifyEventInfo(EventNotifyDetailInfoRequest notifyEventInfo) {
		this.notifyEventInfo = notifyEventInfo;
	}

	public MailNotifyDetailInfoRequest getNotifyMailInfo() {
		return notifyMailInfo;
	}

	public void setNotifyMailInfo(MailNotifyDetailInfoRequest notifyMailInfo) {
		this.notifyMailInfo = notifyMailInfo;
	}

	public JobNotifyDetailInfoRequest getNotifyJobInfo() {
		return notifyJobInfo;
	}

	public void setNotifyJobInfo(JobNotifyDetailInfoRequest notifyJobInfo) {
		this.notifyJobInfo = notifyJobInfo;
	}

	public LogEscalateNotifyDetailInfoRequest getNotifyLogEscalateInfo() {
		return notifyLogEscalateInfo;
	}

	public void setNotifyLogEscalateInfo(LogEscalateNotifyDetailInfoRequest notifyLogEscalateInfo) {
		this.notifyLogEscalateInfo = notifyLogEscalateInfo;
	}

	public CommandNotifyDetailInfoRequest getNotifyCommandInfo() {
		return notifyCommandInfo;
	}

	public void setNotifyCommandInfo(CommandNotifyDetailInfoRequest notifyCommandInfo) {
		this.notifyCommandInfo = notifyCommandInfo;
	}

	public InfraNotifyDetailInfoRequest getNotifyInfraInfo() {
		return notifyInfraInfo;
	}

	public void setNotifyInfraInfo(InfraNotifyDetailInfoRequest notifyInfraInfo) {
		this.notifyInfraInfo = notifyInfraInfo;
	}

	public RestNotifyDetailInfoRequest getNotifyRestInfo() {
		return notifyRestInfo;
	}

	public void setNotifyRestInfo(RestNotifyDetailInfoRequest notifyRestInfo) {
		this.notifyRestInfo = notifyRestInfo;
	}

	public MessageNotifyDetailInfoRequest getNotifyMessageInfo() {
		return notifyMessageInfo;
	}

	public void setNotifyMessageInfo(MessageNotifyDetailInfoRequest notifyMessageInfo) {
		this.notifyMessageInfo = notifyMessageInfo;
	}

	public CloudNotifyDetailInfoRequest getNotifyCloudInfo() {
		return notifyCloudInfo;
	}

	public void setNotifyCloudInfo(CloudNotifyDetailInfoRequest notifyCloudInfo) {
		this.notifyCloudInfo = notifyCloudInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
