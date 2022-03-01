/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */


package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.List;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
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

public class AddRpaScenarioOperationResultCreateSettingRequest implements RequestDto {

	public AddRpaScenarioOperationResultCreateSettingRequest() {
	}

	/** シナリオ実績作成設定ID */
	@RestItemName(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID)
	@RestValidateString(notNull=true, type=CheckType.ID, minLen=1, maxLen=64)
	private String scenarioOperationResultCreateSettingId;
	/** 説明　*/
	@RestItemName(MessageConstant.DESCRIPTION)
	private String description;
	/** オーナーロールID */
	@RestItemName(MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(notNull=true, type=CheckType.ID, minLen=1, maxLen=64)
	private String ownerRoleId;
	/** ファシリティID(スコープ)　*/
	@RestItemName(MessageConstant.SCOPE)
	@RestValidateString(notNull=true, type=CheckType.ID, minLen=1, maxLen=64)
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



	/** シナリオ実績作成設定ID */
	public String getScenarioOperationResultCreateSettingId() {
		return this.scenarioOperationResultCreateSettingId;
	}

	public void setScenarioOperationResultCreateSettingId(String scenarioOperationResultCreateSettingId) {
		this.scenarioOperationResultCreateSettingId = scenarioOperationResultCreateSettingId;
	}


	/** オーナーロールID */
	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	/** カレンダID	*/
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}


	/** 作成対象日(from)	*/
	public String getCreateFromDate() {
		return this.createFromDate;
	}

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
		return "AddRpaScenarioOperationResultCreateSettingRequest [scenarioOperationResultCreateSettingId=" + scenarioOperationResultCreateSettingId + ", description="
				+ description + ", ownerRoleId=" + ownerRoleId + ", facilityId=" + facilityId + ", interval=" + interval + ", calendarId=" + calendarId
				+ ", createFromDate=" + createFromDate + ", notifyId=" + notifyId + ", application=" + application + ", validFlg=" + validFlg + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// ロールIDの存在チェック
		CommonValidator.validateOwnerRoleId(ownerRoleId, false, scenarioOperationResultCreateSettingId, HinemosModuleConstant.RPA_SCENARIO_CREATE);
	}

}
