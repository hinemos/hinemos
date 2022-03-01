/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class JobLinkRcvInfoResponse {

	/** ファシリティID */
	private String facilityID;

	/** スコープ */
	@RestPartiallyTransrateTarget
	private String scope;

	/** 終了値 - 「情報」 */
	private Integer monitorInfoEndValue;

	/** 終了値 - 「警告」 */
	private Integer monitorWarnEndValue;

	/** 終了値 - 「危険」 */
	private Integer monitorCriticalEndValue;

	/** 終了値 - 「不明」 */
	private Integer monitorUnknownEndValue;

	/** メッセージが得られなかった場合に終了する */
	private Boolean failureEndFlg;

	/** メッセージが得られなかった場合に終了する - タイムアウト */
	private Integer monitorWaitTime;

	/** メッセージが得られなかった場合に終了する - 終了値 */
	private Integer monitorWaitEndValue;

	/** ジョブ連携メッセージID */
	private String joblinkMessageId;

	/** メッセージ */
	private String message;

	/** 確認期間フラグ */
	private Boolean pastFlg;

	/** 確認期間（分） */
	private Integer pastMin;

	/** 重要度（情報）有効/無効 */
	private Boolean infoValidFlg;

	/** 重要度（警告）有効/無効 */
	private Boolean warnValidFlg;

	/** 重要度（危険）有効/無効 */
	private Boolean criticalValidFlg;

	/** 重要度（不明）有効/無効 */
	private Boolean unknownValidFlg;

	/** アプリケーションフラグ */
	private Boolean applicationFlg;

	/** アプリケーション */
	private String application;

	/** 監視詳細フラグ */
	private Boolean monitorDetailIdFlg;

	/** 監視詳細 */
	private String monitorDetailId;

	/** メッセージフラグ */
	private Boolean messageFlg;

	/** 拡張情報フラグ */
	private Boolean expFlg;

	/** 終了値 - 「常に」フラグ */
	private Boolean monitorAllEndValueFlg;

	/** 終了値 - 「常に」 */
	private Integer monitorAllEndValue;

	/** ジョブ連携メッセージの拡張情報設定 */
	private ArrayList<JobLinkExpInfoResponse> jobLinkExpList;

	/** メッセージの引継ぎ情報設定 */
	private ArrayList<JobLinkInheritInfoResponse> jobLinkInheritList;

	/**
	 * ファシリティIDを返す。<BR>
	 * @return ファシリティID
	 */
	public String getFacilityID() {
		return facilityID;
	}

	/**
	 * ファシリティIDを設定する。<BR>
	 * @param facilityID ファシリティID
	 */
	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	/**
	 * スコープを返す。<BR>
	 * @return スコープ
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * スコープを設定する。<BR>
	 * @param scope スコープ
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 *  終了値 - 「情報」を返す。<BR>
	 * @return  終了値 - 「情報」
	 */
	public Integer getMonitorInfoEndValue() {
		return monitorInfoEndValue;
	}

	/**
	 *  終了値 - 「情報」を設定する。<BR>
	 * @param monitorInfoEndValue  終了値 - 「情報」
	 */
	public void setMonitorInfoEndValue(Integer monitorInfoEndValue) {
		this.monitorInfoEndValue = monitorInfoEndValue;
	}

	/**
	 *  終了値 - 「警告」を返す。<BR>
	 * @return 終了値 - 「警告」
	 */
	public Integer getMonitorWarnEndValue() {
		return monitorWarnEndValue;
	}

	/**
	 * 終了値 - 「警告」を設定する。<BR>
	 * @param monitorWarnEndValue 終了値 - 「警告」
	 */
	public void setMonitorWarnEndValue(Integer monitorWarnEndValue) {
		this.monitorWarnEndValue = monitorWarnEndValue;
	}

	/**
	 * 終了値 - 「危険」を返す。<BR>
	 * @return 終了値 - 「危険」
	 */
	public Integer getMonitorCriticalEndValue() {
		return monitorCriticalEndValue;
	}

	/**
	 * 終了値 - 「危険」を設定する。<BR>
	 * @param monitorCriticalEndValue 終了値 - 「危険」
	 */
	public void setMonitorCriticalEndValue(Integer monitorCriticalEndValue) {
		this.monitorCriticalEndValue = monitorCriticalEndValue;
	}

	/**
	 * 終了値 - 「不明」を返す。<BR>
	 * @return 終了値 - 「不明」
	 */
	public Integer getMonitorUnknownEndValue() {
		return monitorUnknownEndValue;
	}

	/**
	 * 終了値 - 「不明」を設定する。<BR>
	 * @param monitorUnknownEndValue 終了値 - 「不明」
	 */
	public void setMonitorUnknownEndValue(Integer monitorUnknownEndValue) {
		this.monitorUnknownEndValue = monitorUnknownEndValue;
	}

	/**
	 * メッセージが得られなかった場合に終了する<BR>
	 * @return true:メッセージが得られなかった場合に終了する
	 */
	public Boolean getFailureEndFlg() {
		return failureEndFlg;
	}

	/**
	 * メッセージが得られなかった場合に終了する<BR>
	 * @param failureEndFlg true:メッセージが得られなかった場合に終了する
	 */
	public void setFailureEndFlg(Boolean failureEndFlg) {
		this.failureEndFlg = failureEndFlg;
	}

	/**
	 * メッセージが得られなかった場合に終了する - タイムアウトを返す。<BR>
	 * @return メッセージが得られなかった場合に終了する - タイムアウト
	 */
	public Integer getMonitorWaitTime() {
		return monitorWaitTime;
	}

	/**
	 * メッセージが得られなかった場合に終了する - タイムアウトを設定する。<BR>
	 * @param monitorWaitTime メッセージが得られなかった場合に終了する - タイムアウト
	 */
	public void setMonitorWaitTime(Integer monitorWaitTime) {
		this.monitorWaitTime = monitorWaitTime;
	}

	/**
	 * メッセージが得られなかった場合に終了する - 終了値を返す。<BR>
	 * @return メッセージが得られなかった場合に終了する - 終了値
	 */
	public Integer getMonitorWaitEndValue() {
		return monitorWaitEndValue;
	}

	/**
	 * メッセージが得られなかった場合に終了する - 終了値を設定する。<BR>
	 * @param monitorWaitEndValue メッセージが得られなかった場合に終了する - 終了値
	 */
	public void setMonitorWaitEndValue(Integer monitorWaitEndValue) {
		this.monitorWaitEndValue = monitorWaitEndValue;
	}

	/**
	 * ジョブ連携メッセージIDを返す。<BR>
	 * @return ジョブ連携メッセージID
	 */
	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	/**
	 * ジョブ連携メッセージIDを設定する。<BR>
	 * @param joblinkMessageId ジョブ連携メッセージID
	 */
	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	/**
	 * メッセージを返す。<BR>
	 * @return メッセージ
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * メッセージを設定する。<BR>
	 * @param message メッセージ
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 確認期間フラグを返す。<BR>
	 * @return 確認期間フラグ
	 */
	public Boolean getPastFlg() {
		return pastFlg;
	}

	/**
	 * 確認期間フラグを設定する。<BR>
	 * @param pastFlg 確認期間フラグ
	 */
	public void setPastFlg(Boolean pastFlg) {
		this.pastFlg = pastFlg;
	}

	/**
	 * 確認期間（分）を返す。<BR>
	 * @return 確認期間（分）
	 */
	public Integer getPastMin() {
		return pastMin;
	}

	/**
	 * 確認期間（分）を設定する。<BR>
	 * @param pastMin 確認期間（分）
	 */
	public void setPastMin(Integer pastMin) {
		this.pastMin = pastMin;
	}

	/**
	 * 重要度（情報）有効/無効を返す。<BR>
	 * @return 重要度（情報）有効/無効
	 */
	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}

	/**
	 * 重要度（情報）有効/無効を設定する。<BR>
	 * @param infoValidFlg 重要度（情報）有効/無効
	 */
	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	/**
	 * 重要度（警告）有効/無効を返す。<BR>
	 * @return 重要度（警告）有効/無効
	 */
	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}

	/**
	 * 重要度（警告）有効/無効を設定する。<BR>
	 * @param warnValidFlg 重要度（警告）有効/無効
	 */
	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	/**
	 * 重要度（危険）有効/無効を返す。<BR>
	 * @return 重要度（危険）有効/無効
	 */
	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}

	/**
	 * 重要度（危険）有効/無効を設定する。<BR>
	 * @param criticalValidFlg 重要度（危険）有効/無効
	 */
	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	/**
	 * 重要度（不明）有効/無効を返す。<BR>
	 * @return 重要度（不明）有効/無効
	 */
	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}

	/**
	 * 重要度（不明）有効/無効を設定する。<BR>
	 * @param unknownValidFlg 重要度（不明）有効/無効
	 */
	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	/**
	 * アプリケーションフラグを返す。<BR>
	 * @return アプリケーションフラグ
	 */
	public Boolean getApplicationFlg() {
		return applicationFlg;
	}

	/**
	 * アプリケーションフラグを設定する。<BR>
	 * @param applicationFlg アプリケーションフラグ
	 */
	public void setApplicationFlg(Boolean applicationFlg) {
		this.applicationFlg = applicationFlg;
	}

	/**
	 * アプリケーションを返す。<BR>
	 * @return アプリケーション
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * アプリケーションを設定する。<BR>
	 * @param application アプリケーション
	 */
	public void setApplication(String application) {
		this.application = application;
	}

	/**
	 * 監視詳細フラグを返す。<BR>
	 * @return 監視詳細フラグ
	 */
	public Boolean getMonitorDetailIdFlg() {
		return monitorDetailIdFlg;
	}

	/**
	 * 監視詳細フラグを設定する。<BR>
	 * @param monitorDetailIdFlg 監視詳細フラグ
	 */
	public void setMonitorDetailIdFlg(Boolean monitorDetailIdFlg) {
		this.monitorDetailIdFlg = monitorDetailIdFlg;
	}

	/**
	 * 監視詳細を返す。<BR>
	 * @return 監視詳細
	 */
	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	/**
	 * 監視詳細を設定する。<BR>
	 * @param monitorDetailId 監視詳細
	 */
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	/**
	 * メッセージフラグを返す。<BR>
	 * @return メッセージフラグ
	 */
	public Boolean getMessageFlg() {
		return messageFlg;
	}

	/**
	 * メッセージフラグを設定する。<BR>
	 * @param messageFlg メッセージフラグ
	 */
	public void setMessageFlg(Boolean messageFlg) {
		this.messageFlg = messageFlg;
	}

	/**
	 * 拡張情報フラグを返す。<BR>
	 * @return 拡張情報フラグ
	 */
	public Boolean getExpFlg() {
		return expFlg;
	}

	/**
	 * 拡張情報フラグを設定する。<BR>
	 * @param expFlg 拡張情報フラグ
	 */
	public void setExpFlg(Boolean expFlg) {
		this.expFlg = expFlg;
	}

	/**
	 * 終了値 - 「常に」フラグを返す。<BR>
	 * @return 終了値 - 「常に」フラグ
	 */
	public Boolean getMonitorAllEndValueFlg() {
		return monitorAllEndValueFlg;
	}

	/**
	 * 終了値 - 「常に」フラグを設定する。<BR>
	 * @param monitorAllEndValueFlg 終了値 - 「常に」フラグ
	 */
	public void setMonitorAllEndValueFlg(Boolean monitorAllEndValueFlg) {
		this.monitorAllEndValueFlg = monitorAllEndValueFlg;
	}

	/**
	 * 終了値 - 「常に」 を返す。<BR>
	 * @return  終了値 - 「常に」
	 */
	public Integer getMonitorAllEndValue() {
		return monitorAllEndValue;
	}

	/**
	 *  終了値 - 「常に」を設定する。<BR>
	 * @param monitorAllEndValue  終了値 - 「常に」
	 */
	public void setMonitorAllEndValue(Integer monitorAllEndValue) {
		this.monitorAllEndValue = monitorAllEndValue;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を返す。<BR>
	 * @return ジョブ連携メッセージの拡張情報設定
	 */
	public ArrayList<JobLinkInheritInfoResponse> getJobLinkInheritList() {
		return jobLinkInheritList;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を設定する。<BR>
	 * @param jobLinkInheritList ジョブ連携メッセージの拡張情報設定
	 */
	public void setJobLinkInheritList(ArrayList<JobLinkInheritInfoResponse> jobLinkInheritList) {
		this.jobLinkInheritList = jobLinkInheritList;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を返す。<BR>
	 * @return
	 */
	public ArrayList<JobLinkExpInfoResponse> getJobLinkExpList() {
		return jobLinkExpList;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を設定する。<BR>
	 * @param jobLinkJobExpList
	 */
	public void setJobLinkExpList(ArrayList<JobLinkExpInfoResponse> jobLinkExpList) {
		this.jobLinkExpList = jobLinkExpList;
	}
}
