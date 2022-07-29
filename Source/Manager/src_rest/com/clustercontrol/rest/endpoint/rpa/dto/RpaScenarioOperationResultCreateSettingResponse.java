/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;
import com.clustercontrol.rest.endpoint.rpa.dto.enumtype.ScenarioCreateIntervalEnum;


public class RpaScenarioOperationResultCreateSettingResponse {
	
	public RpaScenarioOperationResultCreateSettingResponse() {
	}

	/** シナリオ実績作成設定ID */
	private String scenarioOperationResultCreateSettingId;
	/** オーナーロールID */
	private String ownerRoleId;
	/** カレンダID	*/
	private String calendarId;
	/** 作成対象日時(from) */
	@RestBeanConvertDatetime
	private String createFromDate;
	/** 説明　*/
	@RestPartiallyTransrateTarget
	private String description;
	/** ファシリティID(スコープ)　*/
	private String facilityId;
	/** スコープ名(ファシリティパス) */
	@RestPartiallyTransrateTarget
	private String scope;
	/** 作成間隔　*/
	@RestBeanConvertEnum
	private ScenarioCreateIntervalEnum interval;
	/** 作成日時 */
	@RestBeanConvertDatetime
	private String regDate;
	/** 新規作成ユーザ */
	private String regUser;
	/** 最終変更日時 */
	@RestBeanConvertDatetime
	private String updateDate;
	/** 最終変更ユーザ */
	private String updateUser;
	/** 通知ID */
	private List<NotifyRelationInfoResponse> notifyId;
	/** アプリケーション */
	private String application;
	/** この設定を有効にする*/
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

	/** 通知ID */
	public List<NotifyRelationInfoResponse> getNotifyId() {
		return notifyId;
	}

	public void setNotifyId(List<NotifyRelationInfoResponse> notifyId) {
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

	/** 作成日時 */
	public String getRegDate() {
		return this.regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}


	/** 新規作成ユーザ */
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}


	/** 最終変更日時 */
	public String getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}


	/** 最終変更ユーザ */
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	/** スコープ名(ファシリティパス) */
	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

}
