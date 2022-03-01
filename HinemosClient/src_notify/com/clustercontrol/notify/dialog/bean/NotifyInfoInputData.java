/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.dialog.bean;

import org.openapitools.client.model.CloudNotifyDetailInfoResponse;
import org.openapitools.client.model.CommandNotifyDetailInfoResponse;
import org.openapitools.client.model.EventNotifyDetailInfoResponse;
import org.openapitools.client.model.InfraNotifyDetailInfoResponse;
import org.openapitools.client.model.JobNotifyDetailInfoResponse;
import org.openapitools.client.model.LogEscalateNotifyDetailInfoResponse;
import org.openapitools.client.model.MailNotifyDetailInfoResponse;
import org.openapitools.client.model.MessageNotifyDetailInfoResponse;
import org.openapitools.client.model.RestNotifyDetailInfoResponse;
import org.openapitools.client.model.StatusNotifyDetailInfoResponse;

/**
 * ダイアログ入力用に通知設定の共通情報を保持するクラス
 */
public class NotifyInfoInputData {
	
	private String notifyId;

	private String description;

	private Integer notifyType;

	private Boolean validFlg;

	private String regDate;

	private String regUser;

	private String updateDate;

	private String updateUser;

	private Integer initialCount;

	private Boolean notFirstNotify;

	private Integer renotifyType;

	private Integer renotifyPeriod;

	private String ownerRoleId;

	private String calendarId;

	private StatusNotifyDetailInfoResponse notifyStatusInfo;

	private EventNotifyDetailInfoResponse notifyEventInfo;

	private MailNotifyDetailInfoResponse notifyMailInfo;

	private JobNotifyDetailInfoResponse notifyJobInfo;

	private LogEscalateNotifyDetailInfoResponse notifyLogEscalateInfo;

	private CommandNotifyDetailInfoResponse notifyCommandInfo;

	private InfraNotifyDetailInfoResponse notifyInfraInfo;

	private RestNotifyDetailInfoResponse notifyRestInfo;
	
	private CloudNotifyDetailInfoResponse notifyCloudInfo;

	private MessageNotifyDetailInfoResponse notifyMessageInfo;

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

	public Integer getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(Integer notifyType) {
		this.notifyType = notifyType;
	}

	public Boolean isValidFlg() {
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

	public Boolean isNotFirstNotify() {
		return notFirstNotify;
	}

	public void setNotFirstNotify(Boolean notFirstNotify) {
		this.notFirstNotify = notFirstNotify;
	}

	public Integer getRenotifyType() {
		return renotifyType;
	}

	public void setRenotifyType(Integer renotifyType) {
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

	public StatusNotifyDetailInfoResponse getNotifyStatusInfo() {
		return notifyStatusInfo;
	}

	public void setNotifyStatusInfo(StatusNotifyDetailInfoResponse notifyStatusInfo) {
		this.notifyStatusInfo = notifyStatusInfo;
	}

	public EventNotifyDetailInfoResponse getNotifyEventInfo() {
		return notifyEventInfo;
	}

	public void setNotifyEventInfo(EventNotifyDetailInfoResponse notifyEventInfo) {
		this.notifyEventInfo = notifyEventInfo;
	}

	public MailNotifyDetailInfoResponse getNotifyMailInfo() {
		return notifyMailInfo;
	}

	public void setNotifyMailInfo(MailNotifyDetailInfoResponse notifyMailInfo) {
		this.notifyMailInfo = notifyMailInfo;
	}

	public JobNotifyDetailInfoResponse getNotifyJobInfo() {
		return notifyJobInfo;
	}

	public void setNotifyJobInfo(JobNotifyDetailInfoResponse notifyJobInfo) {
		this.notifyJobInfo = notifyJobInfo;
	}

	public LogEscalateNotifyDetailInfoResponse getNotifyLogEscalateInfo() {
		return notifyLogEscalateInfo;
	}

	public void setNotifyLogEscalateInfo(LogEscalateNotifyDetailInfoResponse notifyLogEscalateInfo) {
		this.notifyLogEscalateInfo = notifyLogEscalateInfo;
	}

	public CommandNotifyDetailInfoResponse getNotifyCommandInfo() {
		return notifyCommandInfo;
	}

	public void setNotifyCommandInfo(CommandNotifyDetailInfoResponse notifyCommandInfo) {
		this.notifyCommandInfo = notifyCommandInfo;
	}

	public InfraNotifyDetailInfoResponse getNotifyInfraInfo() {
		return notifyInfraInfo;
	}

	public void setNotifyInfraInfo(InfraNotifyDetailInfoResponse notifyInfraInfo) {
		this.notifyInfraInfo = notifyInfraInfo;
	}

	public RestNotifyDetailInfoResponse getNotifyRestInfo() {
		return notifyRestInfo;
	}

	public void setNotifyRestInfo(RestNotifyDetailInfoResponse notifyRestInfo) {
		this.notifyRestInfo = notifyRestInfo;
	}
	
	public CloudNotifyDetailInfoResponse getNotifyCloudInfo() {
		return notifyCloudInfo;
	}

	public void setNotifyCloudInfo(CloudNotifyDetailInfoResponse notifyCloudInfo) {
		this.notifyCloudInfo = notifyCloudInfo;
	}

	public MessageNotifyDetailInfoResponse getNotifyMessageInfo() {
		return notifyMessageInfo;
	}

	public void setNotifyMessageInfo(MessageNotifyDetailInfoResponse notifyMessageInfo) {
		this.notifyMessageInfo = notifyMessageInfo;
	}
}
