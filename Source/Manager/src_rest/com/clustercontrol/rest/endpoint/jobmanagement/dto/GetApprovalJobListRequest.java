/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobApprovalResultEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobApprovalStatusEnum;

public class GetApprovalJobListRequest implements RequestDto {

	@RestBeanConvertDatetime
	private String startFromDate = null;

	@RestBeanConvertDatetime
	private String startToDate = null;

	@RestBeanConvertDatetime
	private String endFromDate = null;

	@RestBeanConvertDatetime
	private String endToDate = null;

	//RestClientBeanUtilでコンバートエラーとなるので、コンバートされないよう名称を変えている
	@RestBeanConvertEnum
	private JobApprovalStatusEnum[] targetStatusList = null;

	@RestBeanConvertEnum
	private JobApprovalResultEnum result = null;

	private String sessionId = null;

	private String jobunitId = null;

	private String jobId = null;

	private String jobName = null;

	private String requestUser = null;

	private String approvalUser = null;

	private String requestSentence;

	private String comment;

	// 取得されるレコードの上限数
	@RestValidateInteger(notNull = false,minVal=0)
	private Integer size ;

	public String getStartFromDate() {
		return startFromDate;
	}

	public void setStartFromDate(String startFromDate) {
		this.startFromDate = startFromDate;
	}

	public String getStartToDate() {
		return startToDate;
	}

	public void setStartToDate(String startToDate) {
		this.startToDate = startToDate;
	}

	public String getEndFromDate() {
		return endFromDate;
	}

	public void setEndFromDate(String endFromDate) {
		this.endFromDate = endFromDate;
	}

	public String getEndToDate() {
		return endToDate;
	}

	public void setEndToDate(String endToDate) {
		this.endToDate = endToDate;
	}

	public JobApprovalStatusEnum[] getTargetStatusList() {
		return targetStatusList;
	}

	public void setTargetStatusList(JobApprovalStatusEnum[] statusList) {
		this.targetStatusList = statusList;
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

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
