/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */


package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;
import com.clustercontrol.rest.endpoint.rpa.dto.enumtype.ScenarioCreateIntervalEnum;
import com.clustercontrol.util.MessageConstant;

public class ModifyRpaScenarioOperationResultCreateSettingRequest implements RequestDto {

	public ModifyRpaScenarioOperationResultCreateSettingRequest() {
	}

	/** 説明　*/
	@RestItemName(MessageConstant.DESCRIPTION)
	private String description;
	/** ファシリティID(スコープ)　*/
	@RestItemName(MessageConstant.SCOPE)
	@RestValidateString(notNull=true, type=CheckType.ID, minLen=1, maxLen=512)
	private String facilityId;
	/** 作成間隔　*/
	@RestItemName(MessageConstant.CREATE_INTERVAL)
	@RestBeanConvertEnum
	@RestValidateObject(notNull=true)
	private ScenarioCreateIntervalEnum interval;
	/** カレンダID	*/
	@RestItemName(MessageConstant.CALENDAR_ID)
	@RestValidateString(type=CheckType.ID, minLen=1, maxLen=64)
	private String calendarId;
	/** 作成対象日時(from) */
	@RestItemName(MessageConstant.CREATE_FROM_DATE)
	@RestValidateString(notNull=true, minLen=1)
	@RestBeanConvertDatetime
	// FIXME 作成対象日時は変更不可。次期メジャーで削除すること。
	private String createFromDate;
	/** 通知ID */
	@RestItemName(value = MessageConstant.NOTIFY_ID)
	private List<NotifyRelationInfoRequest> notifyId;
	/** アプリケーション */
	@RestItemName(MessageConstant.APPLICATION)
	@RestValidateString(minLen=0, maxLen=64)
	private String application;
	/** この設定を有効にする*/
	@RestItemName(MessageConstant.VALID_FLG)
	@RestValidateObject(notNull=true)
	private Boolean validFlg;


	/** カレンダID	*/
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}


	/**
	 * @deprecated 作成対象日時は変更不可
	 */
	// FIXME 次期メジャーで削除すること。
	@Deprecated
	public String getCreateFromDate() {
		return this.createFromDate;
	}

	/**
	 * @deprecated 作成対象日時は変更不可
	 */
	// FIXME 次期メジャーで削除すること。
	@Deprecated
	public void setCreateFromDate(String createFromDate) {
		this.createFromDate = createFromDate;
	}


	/** 説明　*/
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	/** ファシリティID(スコープ)　*/
	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}


	/** 作成間隔　*/
	public ScenarioCreateIntervalEnum getInterval() {
		return this.interval;
	}

	public void setInterval(ScenarioCreateIntervalEnum interval) {
		this.interval = interval;
	}

	/** 通知ID　*/
	public List<NotifyRelationInfoRequest> getNotifyId() {
		return notifyId;
	}
	public void setNotifyId(List<NotifyRelationInfoRequest> notifyId) {
		this.notifyId = notifyId;
	}

	/** アプリケーション */
	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	/** この設定を有効にする*/
	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@Override
	public String toString() {
		return "ModifyRpaScenarioOperationResultCreateSettingRequest [description=" + description + ", facilityId=" + facilityId + ", interval=" + interval
				+ ", calendarId=" + calendarId + ", notifyId=" + notifyId + ", application=" + application + ", validFlg="
				+ validFlg + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
