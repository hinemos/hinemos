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
 * スクリーンショット取得指示情報を保持するクラス
 */
public class RoboScreenshotInfo extends RoboInfo {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 1L;

	private String screenshotFileName;

	/**
	 * コンストラクタ
	 */
	public RoboScreenshotInfo() {
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
	 * @param userName
	 *            ユーザ名
	 * @param screenshotFileName
	 *            取得するスクリーンショットのファイル名
	 */
	public RoboScreenshotInfo(Long datetime, String sessionId, String jobunitId, String jobId, String facilityId, String userName,
			String screenshotFileName) {
		super(datetime, sessionId, jobunitId, jobId, facilityId, userName);
		this.screenshotFileName = screenshotFileName;
	}

	/**
	 * コンストラクタ
	 * 
	 * @param roboInfo
	 *            RPAツールエグゼキューター指示情報
	 * @param screenshotFileName
	 *            取得するスクリーンショットのファイル名
	 */
	public RoboScreenshotInfo(RoboInfo roboInfo, String screenshotFileName) {
		super(roboInfo.getDatetime(), roboInfo.getSessionId(), roboInfo.getJobunitId(), roboInfo.getJobId(),
				roboInfo.getFacilityId(), roboInfo.getUserName());
		this.screenshotFileName = screenshotFileName;
	}

	public String getScreenshotFileName() {
		return screenshotFileName;
	}

	public void setScreenshotFileName(String screenshotFileName) {
		this.screenshotFileName = screenshotFileName;
	}

	@Override
	public String toString() {
		return "RoboScreenshotInfo ["
				+ "toString()=" + super.toString()
				+ ", screenshotFileName=" + screenshotFileName
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((screenshotFileName == null) ? 0 : screenshotFileName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoboScreenshotInfo other = (RoboScreenshotInfo) obj;
		if (screenshotFileName == null) {
			if (other.screenshotFileName != null)
				return false;
		} else if (!screenshotFileName.equals(other.screenshotFileName))
			return false;
		return true;
	}

}
