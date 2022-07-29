/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.bean;

/**
 * RPAツールエグゼキューターが出力するシナリオ実行結果を保持するクラス
 */
public class RoboResultInfo extends RoboInfo {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 1L;
	/**
	 * RPAシナリオのリターンコード<br>
	 * 途中でジョブを停止した場合はnullになります。
	 */
	private Integer returnCode;
	/**
	 * スクリーンショットのファイル名<br>
	 * スクリーンショットが取得されなかった場合はnullになります。
	 */
	private String screenshotFileName;
	/** RPAシナリオ実行で異常終了を示すリターンコードが返された場合のフラグ */
	private Boolean abnormal = false;
	/** RPAシナリオ実行でRPAツールの起動に失敗した場合のフラグ */
	private Boolean error = false;
	/** RPAシナリオ実行でRPAツールの起動に失敗した場合のエラーメッセージ */
	private String errorMessage;

	/**
	 * コンストラクタ
	 */
	public RoboResultInfo() {
	}

	/**
	 * コンストラクタ
	 * 
	 * @param datetime
	 *            RPAシナリオ終了日時
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
	 * @param returnCode
	 *            RPAツールのリターンコード
	 */
	public RoboResultInfo(Long datetime, String sessionId, String jobunitId, String jobId, String facilityId, String userName,
			Integer returnCode) {
		super(datetime, sessionId, jobunitId, jobId, facilityId, userName);
		this.returnCode = returnCode;
	}

	/**
	 * コンストラクタ
	 * 
	 * @param roboInfo
	 *            RPAツールエグゼキューター指示情報
	 */
	public RoboResultInfo(RoboInfo roboInfo) {
		super(roboInfo.getDatetime(), roboInfo.getSessionId(), roboInfo.getJobunitId(), roboInfo.getJobId(),
				roboInfo.getFacilityId(), roboInfo.getUserName());
	}

	public Integer getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(Integer returnCode) {
		this.returnCode = returnCode;
	}

	public String getScreenshotFileName() {
		return screenshotFileName;
	}

	public void setScreenshotFileName(String screenshotFileName) {
		this.screenshotFileName = screenshotFileName;
	}

	public Boolean getAbnormal() {
		return abnormal;
	}

	public void setAbnormal(Boolean abnormal) {
		this.abnormal = abnormal;
	}

	public Boolean getError() {
		return error;
	}

	public void setError(Boolean error) {
		this.error = error;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return "RoboResultInfo ["
				+ "toString()=" + super.toString()
				+ ", returnCode=" + returnCode
				+ ", screenshotFileName=" + screenshotFileName
				+ ", abnormal=" + abnormal
				+ ", error=" + error
				+ ", errorMessage=" + errorMessage
				+ "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((abnormal == null) ? 0 : abnormal.hashCode());
		result = prime * result + ((error == null) ? 0 : error.hashCode());
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		result = prime * result + ((returnCode == null) ? 0 : returnCode.hashCode());
		result = prime * result + ((screenshotFileName == null) ? 0 : screenshotFileName.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoboResultInfo other = (RoboResultInfo) obj;
		if (abnormal == null) {
			if (other.abnormal != null)
				return false;
		} else if (!abnormal.equals(other.abnormal))
			return false;
		if (error == null) {
			if (other.error != null)
				return false;
		} else if (!error.equals(other.error))
			return false;
		if (errorMessage == null) {
			if (other.errorMessage != null)
				return false;
		} else if (!errorMessage.equals(other.errorMessage))
			return false;
		if (returnCode == null) {
			if (other.returnCode != null)
				return false;
		} else if (!returnCode.equals(other.returnCode))
			return false;
		if (screenshotFileName == null) {
			if (other.screenshotFileName != null)
				return false;
		} else if (!screenshotFileName.equals(other.screenshotFileName))
			return false;
		return true;
	}
}
