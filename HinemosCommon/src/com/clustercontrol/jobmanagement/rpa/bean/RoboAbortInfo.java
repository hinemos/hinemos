/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.bean;

/**
 * ジョブの停止時のRPAツールエグゼキューター連携用情報を保持するクラス
 */
public class RoboAbortInfo extends RoboInfo {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 1L;

	/**
	 * コンストラクタ
	 */
	public RoboAbortInfo() {
	}

	/**
	 * コンストラクタ
	 * 
	 * @param datetime
	 *            実行指示日時
	 * @param sessionId
	 *            セッションID
	 * @param jobunitId
	 *            ジョブユニットID
	 * @param jobId
	 *            ジョブID
	 * @param facilityId
	 *            ファシリティID
	 */
	public RoboAbortInfo(Long datetime, String sessionId, String jobunitId, String jobId, String facilityId) {
		super(datetime, sessionId, jobunitId, jobId, facilityId);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param roboInfo
	 *            RPAツールエグゼキューター指示情報
	 */
	public RoboAbortInfo(RoboInfo roboInfo) {
		super(roboInfo.getDatetime(), roboInfo.getSessionId(), roboInfo.getJobunitId(), roboInfo.getJobId(),
				roboInfo.getFacilityId());
	}

	@Override
	public String toString() {
		return "RoboAbortInfo [getDatetime()=" + getDatetime() + ", getSessionId()=" + getSessionId()
				+ ", getJobunitId()=" + getJobunitId() + ", getJobId()=" + getJobId() + ", getFacilityId()="
				+ getFacilityId() + "]";
	}
}
