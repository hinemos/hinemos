/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.scenario.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.util.MessageConstant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * シナリオ実績更新の情報を格納するEntityクラス
 */
@Entity
@Table(name="cc_update_rpa_scenario_operation_result_info", schema="setting")
public class UpdateRpaScenarioOperationResultInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/** ID(PK) */
	private Long updateId;
	/** シナリオ識別文字列 */
	private String scenarioIdentifyString;
	/** シナリオ実績作成設定ID */
	private String scenarioOperationResultCreateSettingId;
	/** 訂正対象期間(from) */
	private Long fromDate;
	/** 訂正対象期間(to) */
	private Long toDate;
	/** 変更対象レコード数 */
	private Long numberOfTargetRecords;
	/** 変更ユーザID */
	private String modifyUserId;
	/** 通知グループID*/
	private String notifyGroupId;
	/** アプリケーション */
	private String application;
	/**通知*/
	private List<NotifyRelationInfo> notifyId;

	/** ID(PK) */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="update_id")
	public Long getUpdateId() {
		return updateId;
	}
	public void setUpdateId(Long reviseId) {
		this.updateId = reviseId;
	}
	
	/** シナリオ識別文字列 */
	@Column(name="scenario_identify_string")
	public String getScenarioIdentifyString() {
		return scenarioIdentifyString;
	}
	public void setScenarioIdentifyString(String scenarioIdentifyString) {
		this.scenarioIdentifyString = scenarioIdentifyString;
	}

	/** シナリオ実績作成設定ID */
	@Column(name="scenario_operation_result_create_setting_id")
	public String getScenarioOperationResultCreateSettingId() {
		return scenarioOperationResultCreateSettingId;
	}
	public void setScenarioOperationResultCreateSettingId(String scenarioOperationResultCreateSettingId) {
		this.scenarioOperationResultCreateSettingId = scenarioOperationResultCreateSettingId;
	}

	/** 訂正対象期間(from) */
	@Column(name="from_date")
	public Long getFromDate() {
		return fromDate;
	}
	public void setFromDate(Long fromDate) {
		this.fromDate = fromDate;
	}

	/** 訂正対象期間(to) */
	@Column(name="to_date")
	public Long getToDate() {
		return toDate;
	}
	public void setToDate(Long toDate) {
		this.toDate = toDate;
	}
	/** 変更対象レコード数 */
	@Column(name="number_of_target_records")
	public Long getNumberOfTargetRecords() {
		return numberOfTargetRecords;
	}
	public void setNumberOfTargetRecords(Long numberOfTargetRecords) {
		this.numberOfTargetRecords = numberOfTargetRecords;
	}
	/** 変更ユーザID */
	@Column(name="modify_user_id")
	public String getModifyUserId() {
		return modifyUserId;
	}
	public void setModifyUserId(String modifyUserId) {
		this.modifyUserId = modifyUserId;
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
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}

	/**
	 * 通知用のアプリケーションを取得する。
	 * アプリケーションカラムが空の場合、既定の文字列を返す。
	 */
	@Transient
	public String getNotifyApplication() {
		if (getApplication().isEmpty()) {
			return MessageConstant.RPA_SCENARIO_OPERATION_RESULT_UPDATE_APPLICATION.getMessage();
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

	@Override
	public String toString() {
		return "UpdateRpaScenarioOperationResultInfo [updateId=" + updateId + ", scenarioIdentifyString=" + scenarioIdentifyString
				+ ", scenarioOperationResultCreateSettingId=" + scenarioOperationResultCreateSettingId + ", fromDate=" + fromDate + ", toDate=" + toDate
				+ ", numberOfTargetRecords=" + numberOfTargetRecords + ", modifyUserId=" + modifyUserId + ", notifyGroupId=" + notifyGroupId + ", application="
				+ application + ", notifyId=" + notifyId + "]";
	}

}
