/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

/**
 * RPAシナリオジョブ（直接実行）のスクリーンショット取得情報を保持するクラス
 */
public class RpaJobScreenshot implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 1L;
	/** セッションID */
	private String sessionId;
	/** ジョブユニットID */
	private String jobunitId;
	/** ジョブID */
	private String jobId;
	/** ファシリティID */
	private String facilityId;
	/** ファイル出力日時 */
	private long outputDate;
	/** ファイルデータ */
	private byte[] filedata;
	/** 説明 */
	private String description;
	/** スクリーンショット取得契機 */
	private int triggerType;

	/**
	 * @return セッションIDを返します。
	 */
	public String getSessionId() {
		return sessionId;
	}
	/**
	 * @param sessionId
	 *            セッションIDを設定します。
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	/**
	 * @return ジョブユニットIDを返します。
	 */
	public String getJobunitId() {
		return jobunitId;
	}
	/**
	 * @param jobunitId
	 *            ジョブユニットIDを設定します。
	 */
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}
	/**
	 * @return ジョブIDを返します。
	 */
	public String getJobId() {
		return jobId;
	}
	/**
	 * @param jobId
	 *            ジョブIDを設定します。
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	/**
	 * @return ファシリティIDを返します。
	 */
	public String getFacilityId() {
		return facilityId;
	}
	/**
	 * @param facilityId
	 *            ファシリティIDを設定します。
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	/**
	 * @return ファイル出力日時を返します。
	 */
	public long getOutputDate() {
		return outputDate;
	}
	/**
	 * @param outputDate
	 *            ファイル出力日時を設定します。
	 */
	public void setOutputDate(long outputDate) {
		this.outputDate = outputDate;
	}
	/**
	 * @return ファイルデータを返します。
	 */
	public byte[] getFiledata() {
		return filedata;
	}
	/**
	 * @param filedata
	 *            ファイルデータを設定します。
	 */
	public void setFiledata(byte[] filedata) {
		this.filedata = filedata;
	}
	/**
	 * @return 説明を返します。
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description
	 *            説明を設定します。
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return スクリーンショット取得契機を返します。
	 */
	public int getTriggerType() {
		return triggerType;
	}
	/**
	 * @param triggerType
	 *            スクリーンショット取得契機を設定します。
	 */
	public void setTriggerType(int triggerType) {
		this.triggerType = triggerType;
	}
}
