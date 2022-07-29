/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.bean;

import java.io.Serializable;

/**
 * RPAシナリオジョブでRPAツールエグゼキューターへ渡す情報を保持するクラス
 */
public class RoboInfo implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 1L;
	/**
	 * 以下の日時のタイムスタンプが入ります。<br>
	 * RoboRunInfoの場合 ：ジョブ実行日時<br>
	 * RoboResultInfoの場合：RPAシナリオ終了日時
	 */
	private Long datetime;
	/** セッションID */
	private String sessionId;
	/** ジョブユニットID */
	private String jobunitId;
	/** ジョブID */
	private String jobId;
	/** ファシリティID */
	private String facilityId;
	/** ユーザ名 */
	private String userName;

	/**
	 * コンストラクタ
	 */
	public RoboInfo() {
	}

	/**
	 * コンストラクタ
	 * 
	 * @param datetime
	 *            日時
	 * @param sessionId
	 *            セッションID
	 * @param jobunitId
	 *            ジョブユニットID
	 * @param jobId
	 *            ジョブID
	 * @param facilityId
	 *            ファシリティID
	 * @param rpaKind
	 *            RPAツール種別
	 * @param userName
	 *            ユーザ名
	 */
	public RoboInfo(Long datetime, String sessionId, String jobunitId, String jobId, String facilityId, String userName) {
		this.datetime = datetime;
		this.sessionId = sessionId;
		this.jobunitId = jobunitId;
		this.jobId = jobId;
		this.facilityId = facilityId;
		this.userName = userName;
	}

	public Long getDatetime() {
		return datetime;
	}

	public void setDatetime(Long datetime) {
		this.datetime = datetime;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		return "RoboInfo ["
				+ "datetime=" + datetime
				+ ", sessionId=" + sessionId
				+ ", jobunitId=" + jobunitId
				+ ", jobId=" + jobId
				+ ", facilityId=" + facilityId
				+ ", userName=" + userName
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datetime == null) ? 0 : datetime.hashCode());
		result = prime * result + ((facilityId == null) ? 0 : facilityId.hashCode());
		result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
		result = prime * result + ((jobunitId == null) ? 0 : jobunitId.hashCode());
		result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoboInfo other = (RoboInfo) obj;
		if (datetime == null) {
			if (other.datetime != null)
				return false;
		} else if (!datetime.equals(other.datetime))
			return false;
		if (facilityId == null) {
			if (other.facilityId != null)
				return false;
		} else if (!facilityId.equals(other.facilityId))
			return false;
		if (jobId == null) {
			if (other.jobId != null)
				return false;
		} else if (!jobId.equals(other.jobId))
			return false;
		if (jobunitId == null) {
			if (other.jobunitId != null)
				return false;
		} else if (!jobunitId.equals(other.jobunitId))
			return false;
		if (sessionId == null) {
			if (other.sessionId != null)
				return false;
		} else if (!sessionId.equals(other.sessionId))
			return false;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}
}
