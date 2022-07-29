/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;


/**
 * ジョブ実行契機[ジョブ連携受信]に関する情報を保持するクラス<BR>
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobLinkRcv extends JobKick implements Serializable {

	/**
	 * シリアライズ可能クラスに定義するUID
	 */
	private static final long serialVersionUID = -8631722702562502112L;

	/** 送信元ファシリティID */
	private String facilityId;

	/** 送信元スコープ */
	private String scope;

	/** ジョブ連携メッセージID */
	private String joblinkMessageId;

	/** 重要度（情報） */
	private Boolean infoValidFlg;

	/** 重要度（警告） */
	private Boolean warnValidFlg;

	/** 重要度（危険） */
	private Boolean criticalValidFlg;

	/** 重要度（不明） */
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

	/** メッセージ */
	private String message;

	/** 拡張情報フラグ */
	private Boolean expFlg;

	/** ジョブ連携メッセージの拡張情報設定 */
	private ArrayList<JobLinkExpInfo> jobLinkExpList;

	/** 確認済みメッセージ番号 */
	private Long joblinkRcvCheckedPosition;

	public JobLinkRcv() {
		this.type = JobKickConstant.TYPE_JOBLINKRCV;
	}

	/**
	 * 送信元スコープを返す<BR>
	 * @return 送信元スコープ
	 */
	public String getFacilityId() {
		return facilityId;
	}

	/**
	 * 送信元スコープを設定する<BR>
	 * @param facilityId 送信元スコープ
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * 送信元スコープを返す<BR>
	 * @return 送信元スコープ
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * 送信元スコープを設定する<BR>
	 * @param scope スコープ
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * ジョブ連携メッセージIDを返す<BR>
	 * @return ジョブ連携メッセージID
	 */
	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	/**
	 * ジョブ連携メッセージIDを設定する<BR>
	 * @param joblinkMessageId ジョブ連携メッセージID
	 */
	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	/**
	 * 重要度（情報）を返す<BR>
	 * @return 重要度（情報）
	 */
	public Boolean getInfoValidFlg() {
		return infoValidFlg;
	}

	/**
	 * 重要度（情報）を設定する<BR>
	 * @param infoValidFlg 重要度（情報）
	 */
	public void setInfoValidFlg(Boolean infoValidFlg) {
		this.infoValidFlg = infoValidFlg;
	}

	/**
	 * 重要度（警告）を返す<BR>
	 * @return 重要度（警告）
	 */
	public Boolean getWarnValidFlg() {
		return warnValidFlg;
	}

	/**
	 * 重要度（警告）を設定する<BR>
	 * @param warnValidFlg 重要度（警告）
	 */
	public void setWarnValidFlg(Boolean warnValidFlg) {
		this.warnValidFlg = warnValidFlg;
	}

	/**
	 * 重要度（危険）を返す<BR>
	 * @return 重要度（危険）
	 */
	public Boolean getCriticalValidFlg() {
		return criticalValidFlg;
	}

	/**
	 * 重要度（危険）を設定する<BR>
	 * @param criticalValidFlg 重要度（危険）
	 */
	public void setCriticalValidFlg(Boolean criticalValidFlg) {
		this.criticalValidFlg = criticalValidFlg;
	}

	/**
	 * 重要度（不明）を返す<BR>
	 * @return 重要度（不明）
	 */
	public Boolean getUnknownValidFlg() {
		return unknownValidFlg;
	}

	/**
	 * 重要度（不明）を設定する<BR>
	 * @param unknownValidFlg 重要度（不明）
	 */
	public void setUnknownValidFlg(Boolean unknownValidFlg) {
		this.unknownValidFlg = unknownValidFlg;
	}

	/**
	 * アプリケーションフラグを返す<BR>
	 * @return アプリケーションフラグ
	 */
	public Boolean getApplicationFlg() {
		return applicationFlg;
	}

	/**
	 * アプリケーションフラグを設定する<BR>
	 * @param joblinkRcvApplicationFlg アプリケーションフラグ
	 */
	public void setApplicationFlg(Boolean applicationFlg) {
		this.applicationFlg = applicationFlg;
	}

	/**
	 * アプリケーションを返す<BR>
	 * @return アプリケーション
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * アプリケーションを設定する<BR>
	 * @param application アプリケーション
	 */
	public void setApplication(String application) {
		this.application = application;
	}

	/**
	 * 監視詳細フラグを返す<BR>
	 * @return 監視詳細フラグ
	 */
	public Boolean getMonitorDetailIdFlg() {
		return monitorDetailIdFlg;
	}

	/**
	 * 監視詳細フラグを設定する<BR>
	 * @param monitorDetailIdFlg 監視詳細フラグ
	 */
	public void setMonitorDetailIdFlg(Boolean monitorDetailIdFlg) {
		this.monitorDetailIdFlg = monitorDetailIdFlg;
	}

	/**
	 * 監視詳細を返す<BR>
	 * @return 監視詳細
	 */
	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	/**
	 * 監視詳細を設定する<BR>
	 * @param monitorDetailId 監視詳細
	 */
	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	/**
	 * メッセージフラグを返す<BR>
	 * @return メッセージフラグ
	 */
	public Boolean getMessageFlg() {
		return messageFlg;
	}

	/**
	 * メッセージフラグを設定する<BR>
	 * @param messageFlg メッセージフラグ
	 */
	public void setMessageFlg(Boolean messageFlg) {
		this.messageFlg = messageFlg;
	}

	/**
	 * メッセージを返す<BR>
	 * @return メッセージ
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * メッセージを設定する<BR>
	 * @param message メッセージ
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 拡張情報フラグを返す<BR>
	 * @return 拡張情報フラグ
	 */
	public Boolean getExpFlg() {
		return expFlg;
	}

	/**
	 * 拡張情報フラグを設定する<BR>
	 * @param expFlg 拡張情報フラグ
	 */
	public void setExpFlg(Boolean expFlg) {
		this.expFlg = expFlg;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を返す<BR>
	 * @return ジョブ連携メッセージの拡張情報設定
	 */
	public ArrayList<JobLinkExpInfo> getJobLinkExpList() {
		return jobLinkExpList;
	}

	/**
	 * ジョブ連携メッセージの拡張情報設定を設定する<BR>
	 * @param jobLinkExpList ジョブ連携メッセージの拡張情報設定
	 */
	public void setJobLinkExpList(ArrayList<JobLinkExpInfo> jobLinkExpList) {
		this.jobLinkExpList = jobLinkExpList;
	}

	/**
	 * 確認済みメッセージ番号を返す<BR>
	 * @return 確認済みメッセージ番号
	 */
	public Long getJoblinkRcvCheckedPosition() {
		return joblinkRcvCheckedPosition;
	}

	/**
	 * 確認済みメッセージ番号を設定する<BR>
	 * @param joblinkRcvCheckedPosition 確認済みメッセージ番号
	 */
	public void setJoblinkRcvCheckedPosition(Long joblinkRcvCheckedPosition) {
		this.joblinkRcvCheckedPosition = joblinkRcvCheckedPosition;
	}

	@Override
	public String toString() {
		return "JobLinkRcv ["
				+ "facilityId=" + facilityId
				+ ", scope=" + scope
				+ ", joblinkMessageId=" + joblinkMessageId
				+ ", infoValidFlg=" + infoValidFlg
				+ ", warnValidFlg=" + warnValidFlg
				+ ", criticalValidFlg=" + criticalValidFlg
				+ ", unknownValidFlg=" + unknownValidFlg
				+ ", applicationFlg=" + applicationFlg
				+ ", application=" + application
				+ ", monitorDetailIdFlg=" + monitorDetailIdFlg
				+ ", monitorDetailId=" + monitorDetailId
				+ ", messageFlg=" + messageFlg
				+ ", message=" + message
				+ ", expFlg=" + expFlg
				+ ", jobLinkExpList=" + jobLinkExpList
				+ ", joblinkRcvCheckedPosition=" + joblinkRcvCheckedPosition + "]";
	}
}