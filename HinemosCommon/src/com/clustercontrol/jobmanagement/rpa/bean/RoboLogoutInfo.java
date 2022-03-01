/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.bean;

/**
 * RPAシナリオジョブでRPAツールエグゼキューターへ渡す<br>
 * ログアウト指示情報を保持するクラス
 */
public class RoboLogoutInfo extends RoboInfo {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 1L;

	/**
	 * コンストラクタ
	 */
	public RoboLogoutInfo() {
	}

	/**
	 * コンストラクタ
	 * 
	 * @param datetime
	 *            実行日時
	 * @param sessionId
	 *            セッションID
	 * @param jobunitId
	 *            ジョブユニットID
	 * @param jobId
	 *            ジョブID
	 * @param facilityId
	 *            ファシリティID
	 */
	public RoboLogoutInfo(Long datetime, String sessionId, String jobunitId, String jobId, String facilityId) {
		super(datetime, sessionId, jobunitId, jobId, facilityId);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param roboInfo
	 *            RPAツールエグゼキューター指示情報
	 */
	public RoboLogoutInfo(RoboInfo roboInfo) {
		super(roboInfo.getDatetime(), roboInfo.getSessionId(), roboInfo.getJobunitId(), roboInfo.getJobId(),
				roboInfo.getFacilityId());
	}

	@Override
	public String toString() {
		return "RoboLogoutInfo [getDatetime()=" + getDatetime() + ", getSessionId()=" + getSessionId()
				+ ", getJobunitId()=" + getJobunitId() + ", getJobId()=" + getJobId() + ", getFacilityId()="
				+ getFacilityId() + ", getLogoutFileName()=" + "]";
	}
}
