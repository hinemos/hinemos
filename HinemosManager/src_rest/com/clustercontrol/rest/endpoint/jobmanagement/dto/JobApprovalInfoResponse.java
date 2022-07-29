/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobApprovalResultEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobApprovalStatusEnum;

public class JobApprovalInfoResponse {

	/** 承認状態 */
	@RestBeanConvertEnum
	private JobApprovalStatusEnum status = null;

	/** 承認結果 */
	@RestBeanConvertEnum
	private JobApprovalResultEnum result = null;

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
	@RestBeanConvertDatetime
	private String startDate;

	/** 承認完了日時 */
	@RestBeanConvertDatetime
	private String endDate;

	/** 承認依頼文 */
	private String requestSentence;

	/** コメント */
	private String comment;

	public JobApprovalStatusEnum getStatus() {
		return status;
	}

	public void setStatus(JobApprovalStatusEnum status) {
		this.status = status;
	}

	public JobApprovalResultEnum getResult() {
		return result;
	}

	public void setResult(JobApprovalResultEnum result) {
		this.result = result;
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

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getRequestUser() {
		return requestUser;
	}

	public void setRequestUser(String requestUser) {
		this.requestUser = requestUser;
	}

	public String getApprovalUser() {
		return approvalUser;
	}

	public void setApprovalUser(String approvalUser) {
		this.approvalUser = approvalUser;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getRequestSentence() {
		return requestSentence;
	}

	public void setRequestSentence(String requestSentence) {
		this.requestSentence = requestSentence;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
