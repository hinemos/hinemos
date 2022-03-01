/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


/**
 * シナリオ実績作成設定のEntity定義
 *
 */
@Entity
@Table(name="cc_rpa_scenario_operation_result_create_setting", schema="setting")
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.RPA_SCENARIO_CREATE,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="scenario_operation_result_create_setting_id", insertable=false, updatable=false))
public class RpaScenarioOperationResultCreateSetting extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	/** シナリオ実績作成設定ID */
	private String scenarioOperationResultCreateSettingId;
	/** カレンダID	*/
	private String calendarId;
	/** 作成対象日時(from) */
	private Long createFromDate;
	/** 説明　*/
	private String description;
	/** ファシリティID(スコープ)　*/
	private String facilityId;
	/** 作成間隔　*/
	private Integer interval;
	/** 通知グループID*/
	private String notifyGroupId;
	/** アプリケーション */
	private String application;
	/** この設定を有効にする*/
	private Boolean validFlg;
	/** 作成日時 */
	private Long regDate;
	/** 新規作成ユーザ */
	private String regUser;
	/** 最終変更日時 */
	private Long updateDate;
	/** 最終変更ユーザ */
	private String updateUser;
	
	/** スコープ名(ファシリティパス) */
	private String scope;
	
	/**通知*/
	private List<NotifyRelationInfo> notifyId;

	/**
	 * シナリオIDに含まれる連番
	 * シナリオID:「シナリオ実績作成設定ID_scenarioNumber」 
	 */
	private Long scenarioNumber = 1L;
	/** シナリオログとして解析した文字列データ(CollectStringData)の最終解析位置(dataId) */
	private Long lastPosition = 0L;

	public RpaScenarioOperationResultCreateSetting() {
	}


	/** シナリオ実績作成設定ID */
	@Id
	@Column(name="scenario_operation_result_create_setting_id")
	public String getScenarioOperationResultCreateSettingId() {
		return this.scenarioOperationResultCreateSettingId;
	}

	public void setScenarioOperationResultCreateSettingId(String scenarioOperationResultCreateSettingId) {
		this.scenarioOperationResultCreateSettingId = scenarioOperationResultCreateSettingId;
	}


	/** カレンダID	*/
	@Column(name="calendar_id")
	public String getCalendarId() {
		return this.calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}


	/** 作成対象日(from)	*/
	@Column(name="create_from_date")
	public Long getCreateFromDate() {
		return this.createFromDate;
	}

	public void setCreateFromDate(Long createFromDate) {
		this.createFromDate = createFromDate;
	}


	/** 説明　*/
	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	/** ファシリティID(スコープ)　*/
	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}


	/** 作成間隔　*/
	@Column(name="interval")
	public Integer getInterval() {
		return this.interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}
	
	/** 通知グループID*/
	@Column(name="notify_group_id")
	public String getNotifyGroupId() {
		return notifyGroupId;
	}
	
	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
	}
	
	/** アプリケーション */
	@Column(name="application")
	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}


	/** この設定を有効にする*/
	@Column(name="valid_flg")
	public Boolean getValidFlg() {
		return validFlg;
	}


	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}


	/** 作成日時 */
	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}


	/** 新規作成ユーザ */
	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}


	/** 最終変更日時 */
	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}


	/** 最終変更ユーザ */
	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}


	/**
	 * シナリオIDに含まれる連番
	 * シナリオID:「シナリオ実績作成設定ID_scenarioNumber」 
	 */
	@Column(name="scenario_number")
	public Long getScenarioNumber() {
		return scenarioNumber;
	}


	public void setScenarioNumber(Long scenarioNumber) {
		this.scenarioNumber = scenarioNumber;
	}


	/** シナリオログとして解析した文字列データ(CollectStringData)の最終解析位置(dataId) */
	@Column(name="last_position")
	public Long getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(Long lastPosition) {
		this.lastPosition = lastPosition;
	}

	/** スコープ名(ファシリティパス) */
	@Transient
	public String getScope() {
		if (scope == null)
			try {
				scope = new RepositoryControllerBean().getFacilityPath(getFacilityId(), null);
			} catch (HinemosUnknown e) {
				Logger.getLogger(this.getClass()).debug(e.getMessage(), e);
			}
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
	
	@Transient
	public String getNotifyApplication() {
		if (getApplication().isEmpty()) {
			return MessageConstant.RPA_SCENARIO_CREATE_APPLICATION.getMessage();
		} else {
			return getApplication();
		}
	}


	/**通知*/
	@Transient
	public List<NotifyRelationInfo> getNotifyId() {
		if (notifyId == null) {
			notifyId = new ArrayList<>();
		}
		return notifyId;
	}

	public void setNotifyId(List<NotifyRelationInfo> notifyId) {
		this.notifyId = notifyId;
	}
}