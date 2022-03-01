/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PriorityRequiredEnum;

public class RegistJobLinkMessageRequest implements RequestDto {

	// ジョブ連携送信設定ID
	private String joblinkSendSettingId;

	// 送信先ファシリティID
	private String facilityId;

	// 送信元IPアドレス
	private List<String> sourceIpAddressList;

	// ジョブ連携メッセージID
	private String joblinkMessageId;

	// 送信日時
	@RestBeanConvertDatetime
	private String sendDate;

	// 監視詳細ID
	private String monitorDetailId;

	// アプリケーション
	private String application;

	// 重要度
	@RestBeanConvertEnum
	private PriorityRequiredEnum priority;

	// メッセージ
	private String message;

	// オリジナルメッセージ
	private String messageOrg;

	// 拡張情報
	private ArrayList<JobLinkExpInfoRequest> JobLinkExpInfoList;

	public RegistJobLinkMessageRequest() {
	}

	public String getJoblinkSendSettingId() {
		return joblinkSendSettingId;
	}

	public void setJoblinkSendSettingId(String joblinkSendSettingId) {
		this.joblinkSendSettingId = joblinkSendSettingId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public List<String> getSourceIpAddressList() {
		return sourceIpAddressList;
	}

	public void setSourceIpAddressList(List<String> sourceIpAddressList) {
		this.sourceIpAddressList = sourceIpAddressList;
	}

	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	public String getSendDate() {
		return sendDate;
	}

	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}

	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
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

	public String getMessageOrg() {
		return messageOrg;
	}

	public void setMessageOrg(String messageOrg) {
		this.messageOrg = messageOrg;
	}

	public ArrayList<JobLinkExpInfoRequest> getJobLinkExpInfoList() {
		return JobLinkExpInfoList;
	}

	public void setJobLinkExpInfoList(ArrayList<JobLinkExpInfoRequest> jobLinkExpInfoList) {
		JobLinkExpInfoList = jobLinkExpInfoList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
