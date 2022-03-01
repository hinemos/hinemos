/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;

import com.clustercontrol.jobmanagement.bean.JobLinkSendProtocol;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.EndStatusSelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.OperationFailureEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PriorityRequiredEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;

public class JobLinkSendInfoResponse {

	/** 送信に失敗した場合再送する */
	private Boolean retryFlg;

	/** 再送回数 */
	private Integer retryCount;

	/** 送信失敗時の操作 */
	@RestBeanConvertEnum
	private OperationFailureEnum failureOperation;

	/** 送信失敗時の終了状態 */
	@RestBeanConvertEnum
	private EndStatusSelectEnum failureEndStatus;

	/** ジョブ連携メッセージID */
	private String joblinkMessageId;

	/** 重要度 */
	@RestBeanConvertEnum
	private PriorityRequiredEnum priority;

	/** メッセージ */
	private String message;

	/** 終了値（送信成功） */
	private Integer successEndValue;

	/** 終了値（送信失敗） */
	private Integer failureEndValue;

	/** ジョブ連携送信設定ID */
	private String joblinkSendSettingId;

	/** ファシリティID */
	private String facilityID;

	/** スコープ */
	private String scope;

	/** スコープ処理 */
	@RestBeanConvertEnum
	private ProcessingMethodEnum processingMethod;

	/** 送信先プロトコル */
	@RestBeanConvertEnum
	private JobLinkSendProtocol protocol;

	/** 送信先ポート */
	private Integer port;

	/** HinemosユーザID */
	private String hinemosUserId;

	/** Hinemosパスワード */
	private String hinemosPassword;

	/** HTTP Proxyを使用する */
	private Boolean proxyFlg;

	/** HTTP Proxyホスト */
	private String proxyHost;

	/** HTTP Proxyポート */
	private Integer proxyPort; 

	/** HTTP Proxyユーザ */
	private String proxyUser;

	/** HTTP Proxyパスワード */
	private String proxyPassword;

	/** ジョブ連携メッセージの拡張情報設定 */
	private ArrayList<JobLinkExpInfoResponse> jobLinkExpList;

	public Boolean getRetryFlg() {
		return retryFlg;
	}
	public void setRetryFlg(Boolean retryFlg) {
		this.retryFlg = retryFlg;
	}

	public Integer getRetryCount() {
		return retryCount;
	}
	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public OperationFailureEnum getFailureOperation() {
		return failureOperation;
	}
	public void setFailureOperation(OperationFailureEnum failureOperation) {
		this.failureOperation = failureOperation;
	}

	public EndStatusSelectEnum getFailureEndStatus() {
		return failureEndStatus;
	}

	public void setFailureEndStatus(EndStatusSelectEnum failureEndStatus) {
		this.failureEndStatus = failureEndStatus;
	}

	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}
	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	public String getFacilityID() {
		return facilityID;
	}
	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}

	public ProcessingMethodEnum getProcessingMethod() {
		return processingMethod;
	}
	public void setProcessingMethod(ProcessingMethodEnum processingMethod) {
		this.processingMethod = processingMethod;
	}

	public JobLinkSendProtocol getProtocol() {
		return protocol;
	}
	public void setProtocol(JobLinkSendProtocol protocol) {
		this.protocol = protocol;
	}

	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}

	public String getHinemosUserId() {
		return hinemosUserId;
	}
	public void setHinemosUserId(String hinemosUserId) {
		this.hinemosUserId = hinemosUserId;
	}

	public String getHinemosPassword() {
		return hinemosPassword;
	}
	public void setHinemosPassword(String hinemosPassword) {
		this.hinemosPassword = hinemosPassword;
	}

	public Boolean getProxyFlg() {
		return proxyFlg;
	}
	public void setProxyFlg(Boolean proxyFlg) {
		this.proxyFlg = proxyFlg;
	}

	public String getProxyHost() {
		return proxyHost;
	}
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUser() {
		return proxyUser;
	}
	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}
	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public PriorityRequiredEnum getPriority() {
		return priority;
	}
	public void setPriority(PriorityRequiredEnum priority) {
		this.priority = priority;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getSuccessEndValue() {
		return successEndValue;
	}
	public void setSuccessEndValue(Integer successEndValue) {
		this.successEndValue = successEndValue;
	}

	public Integer getFailureEndValue() {
		return failureEndValue;
	}
	public void setFailureEndValue(Integer failureEndValue) {
		this.failureEndValue = failureEndValue;
	}

	public String getJoblinkSendSettingId() {
		return joblinkSendSettingId;
	}
	public void setJoblinkSendSettingId(String joblinkSendSettingId) {
		this.joblinkSendSettingId = joblinkSendSettingId;
	}

	public ArrayList<JobLinkExpInfoResponse> getJobLinkExpList() {
		return jobLinkExpList;
	}
	public void setJobLinkExpList(ArrayList<JobLinkExpInfoResponse> jobLinkExpList) {
		this.jobLinkExpList = jobLinkExpList;
	}
}
