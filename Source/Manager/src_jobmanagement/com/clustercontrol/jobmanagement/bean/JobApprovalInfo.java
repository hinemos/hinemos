/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;


/**
 * 承認ジョブ情報を保持するクラス<BR>
 * @version 5.1.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobApprovalInfo implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 1L;

	/** マネージャー名 */
	private String mangerName = null;

	/** 承認状態 */
	private Integer status = null;

	/** 承認結果 */
	private Integer result = null;

	/** セッションID */
	private String sessionId = null;

	/** ジョブユニットID */
	private String jobunitId = null;
	
	/** ジョブID */
	private String jobId = null;

	/** ジョブ名 */
	private String jobName = null;
	
	/** 実行ユーザ */
	private String requestUser = null;

	/** 承認ユーザ */
	private String approvalUser = null;

	/** 承認依頼日時 */
	private Long startDate;

	/** 承認完了日時 */
	private Long endDate;

	/** 承認依頼文 */
	private String requestSentence;

	/** コメント */
	private String comment;

	/**
	 * コンストラクタ<BR>
	 */
	public JobApprovalInfo() {}
	
	public JobApprovalInfo(String managerName, Integer status, Integer result, String sessionId, String jobunitId, String jobId,
			String jobName, String requestUser, String approvalUser, Long startDate, Long endDate, String requestSentence, String comment) {
		super();
		this.mangerName = managerName;
		this.status = status;
		this.result = result;
		this.sessionId = sessionId;
		this.jobunitId = jobunitId;
		this.jobId = jobId;
		this.jobName = jobName;
		this.requestUser = requestUser;
		this.approvalUser = approvalUser;
		this.startDate = startDate;
		this.endDate = endDate;
		this.requestSentence = requestSentence;
		this.comment = comment;
	}

	/**
	 * マネージャー名を返す<BR>
	 * @return マネージャー名
	 */
	public String getMangerName() {
		return mangerName;
	}
	/**
	 * マネージャー名を設定する<BR>
	 * @param status マネージャー名
	 */
	public void setMangerName(String managerName) {
		this.mangerName = managerName;
	}

	/**
	 * 承認状態を返す<BR>
	 * @return 承認状態
	 */
	public Integer getStatus() {
		return status;
	}
	/**
	 * 承認状態を設定する<BR>
	 * @param status 承認状態
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * 承認結果を返す<BR>
	 * @return 承認結果
	 */
	public Integer getResult() {
		return result;
	}
	/**
	 * 承認結果を設定する<BR>
	 * @param result 承認結果
	 */
	public void setResult(Integer result) {
		this.result = result;
	}

	/**
	 * セッションIDを返す<BR>
	 * @return セッションID
	 */
	public String getSessionId() {
		return sessionId;
	}
	/**
	 * セッションIDを設定する<BR>
	 * @param sessionId セッションID
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	

	/**
	 * ジョブユニットIDを返す<BR>
	 * @return ジョブユニットID
	 */
	public String getJobunitId() {
		return jobunitId;
	}
	/**
	 * ジョブユニットIDを設定する<BR>
	 * @param jobId ジョブユニットID
	 */
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	/**
	 * ジョブIDを返す<BR>
	 * @return ジョブID
	 */
	public String getJobId() {
		return jobId;
	}
	/**
	 * ジョブIDを設定する<BR>
	 * @param jobId ジョブID
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * ジョブ名を返す<BR>
	 * @return ジョブID
	 */
	public String getJobName() {
		return jobName;
	}
	/**
	 * ジョブ名を設定する<BR>
	 * @param jobId ジョブID
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * 実行ユーザを返す<BR>
	 * @return 実行ユーザ
	 */
	public String getRequestUser() {
		return requestUser;
	}
	/**
	 * 実行ユーザを設定する<BR>
	 * @param requestUser 実行ユーザ
	 */
	public void setRequestUser(String requestUser) {
		this.requestUser = requestUser;
	}

	/**
	 * 承認ユーザを返す<BR>
	 * @return 承認ユーザ
	 */
	public String getApprovalUser() {
		return approvalUser;
	}
	/**
	 * 承認ユーザを設定する<BR>
	 * @param approvalUser 承認ユーザ
	 */
	public void setApprovalUser(String approvalUser) {
		this.approvalUser = approvalUser;
	}

	/**
	 * 承認依頼日時を返す<BR>
	 * @return 承認依頼日時
	 */
	public Long getStartDate() {
		return startDate;
	}
	/**
	 * 承認依頼日時を設定する<BR>
	 * @param createTime 承認依頼日時
	 */
	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}

	/**
	 * 承認完了日時を返す<BR>
	 * @return 承認完了日時
	 */
	public Long getEndDate() {
		return endDate;
	}
	/**
	 * 承認完了日時を設定する<BR>
	 * @param createTime 承認完了日時
	 */
	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}

	/**
	 * 承認依頼文を返す<BR>
	 * @return コメント
	 */
	public String getRequestSentence() {
		return requestSentence;
	}
	/**
	 * 承認依頼文を設定する<BR>
	 * @param requestSentence コメント
	 */
	public void setRequestSentence(String requestSentence) {
		this.requestSentence = requestSentence;
	}

	/**
	 * コメントを返す<BR>
	 * @return コメント
	 */
	public String getComment() {
		return comment;
	}
	/**
	 * コメントを設定する<BR>
	 * @param comment コメント
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
}