/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * 
 * ジョブ連携送信結果を格納するクラスです。
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobLinkMessageInfo implements Serializable {

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -7643986717128485108L;

	// ジョブ連携メッセージID
	private String joblinkMessageId;

	// 送信元ファシリティID
	private String facilityId;

	// 送信日時
	private Long sendDate;

	// 受信日時
	private Long acceptDate;

	// 送信元ファシリティ名
	private String facilityName;

	// 送信元IPアドレス
	private String ipAddress;

	// 監視詳細
	private String monitorDetailId;

	// アプリケーション
	private String application;

	// 重要度
	private Integer priority;

	// メッセージ
	private String message;

	// オリジナルメッセージ
	private String messageOrg;

	// 拡張情報
	private List<JobLinkExpInfo> jobLinkExpInfo = new ArrayList<>();

	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Long getSendDate() {
		return sendDate;
	}

	public void setSendDate(Long sendDate) {
		this.sendDate = sendDate;
	}

	public Long getAcceptDate() {
		return acceptDate;
	}

	public void setAcceptDate(Long acceptDate) {
		this.acceptDate = acceptDate;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
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

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
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

	public List<JobLinkExpInfo> getJobLinkExpInfo() {
		return jobLinkExpInfo;
	}

	public void setJobLinkExpInfo(List<JobLinkExpInfo> jobLinkExpInfo) {
		this.jobLinkExpInfo = jobLinkExpInfo;
	}
}
