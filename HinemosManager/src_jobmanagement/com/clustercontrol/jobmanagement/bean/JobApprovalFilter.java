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
 * 
 * 承認ジョブのフィルタ
 *
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobApprovalFilter implements Serializable{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private Long startFromDate = null;
	private Long startToDate = null;
	private Long endFromDate = null;
	private Long endToDate = null;
	private Integer[] statusList = null;
	private Integer result = null;
	private String sessionId = null;
	private String jobunitId = null;
	private String jobId = null;
	private String jobName = null;
	private String requestUser = null;
	private String approvalUser = null;
	private String requestSentence;
	private String comment;

	public JobApprovalFilter(Long startFromDate, Long startToDate,
			Long endFromDate, Long endToDate, Integer[] statusList, Integer result,
			String sessionId, String jobunitId, String jobId, String jobName, 
			String requestUser, String approvalUser, String requestSentence, String comment) {
		super();
		this.startFromDate = startFromDate;
		this.startToDate = startToDate;
		this.endFromDate = endFromDate;
		this.endToDate = endToDate;
		this.statusList = statusList;
		this.result = result;
		this.sessionId = sessionId;
		this.jobunitId = jobunitId;
		this.jobId = jobId;
		this.jobName = jobName;
		this.requestUser = requestUser;
		this.approvalUser = approvalUser;
		this.requestSentence = requestSentence;
		this.comment = comment;
	}

	public JobApprovalFilter() {}

	public Long getStartFromDate() {
		return startFromDate;
	}

	public void setStartFromDate(Long startFromDate) {
		this.startFromDate = startFromDate;
	}

	public Long getStartToDate() {
		return startToDate;
	}

	public void setStartToDate(Long startToDate) {
		this.startToDate = startToDate;
	}

	public Long getEndFromDate() {
		return endFromDate;
	}

	public void setEndFromDate(Long endFromDate) {
		this.endFromDate = endFromDate;
	}

	public Long getEndToDate() {
		return endToDate;
	}

	public void setEndToDate(Long endToDate) {
		this.endToDate = endToDate;
	}

	public Integer[] getStatusList() {
		return statusList;
	}

	public void setStatusList(Integer[] statusList) {
		this.statusList = statusList;
	}

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
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
