/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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
 * ジョブ連携メッセージ一覧のフィルタ
 *
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobLinkMessageFilter implements Serializable {

	private static final long serialVersionUID = -7664844704711882726L;

	// ジョブ連携メッセージID
	private String joblinkMessageId = null;
	// 送信元ファシリティID
	private String srcFacilityId = null;
	// 送信元ファシリティ名
	private String srcFacilityName = null;
	// 監視詳細
	private String monitorDetailId = null;
	// アプリケーション
	private String application = null;
	// 重要度リスト
	private Integer[] priorityList = null;
	// メッセージ
	private String message = null;
	// 送信日時（From）
	private Long sendFromDate = null;
	// 送信日時（To）
	private Long sendToDate = null;
	// 受信日時（From）
	private Long acceptFromDate = null;
	// 受信日時（To）
	private Long acceptToDate = null;

	public JobLinkMessageFilter(String joblinkMessageId, String srcFacilityId, String srcFacilityName,
			String monitorDetailId, String application, Integer[] priorityList, String message, Long sendFromDate,
			Long sendToDate, Long acceptFromDate, Long acceptToDate) {
		super();
		this.joblinkMessageId = joblinkMessageId;
		this.srcFacilityId = srcFacilityId;
		this.srcFacilityName = srcFacilityName;
		this.monitorDetailId = monitorDetailId;
		this.application = application;
		this.priorityList = priorityList;
		this.message = message;
		this.sendFromDate = sendFromDate;
		this.sendToDate = sendToDate;
		this.acceptFromDate = acceptFromDate;
		this.acceptToDate = acceptToDate;
	}

	public JobLinkMessageFilter() {
	}

	public String getJoblinkMessageId() {
		return joblinkMessageId;
	}

	public void setJoblinkMessageId(String joblinkMessageId) {
		this.joblinkMessageId = joblinkMessageId;
	}

	public String getSrcFacilityId() {
		return srcFacilityId;
	}

	public void setSrcFacilityId(String srcFacilityId) {
		this.srcFacilityId = srcFacilityId;
	}

	public String getSrcFacilityName() {
		return srcFacilityName;
	}

	public void setSrcFacilityName(String srcFacilityName) {
		this.srcFacilityName = srcFacilityName;
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

	public Integer[] getPriorityList() {
		return priorityList;
	}

	public void setPriorityList(Integer[] priorityList) {
		this.priorityList = priorityList;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getSendFromDate() {
		return sendFromDate;
	}

	public void setSendFromDate(Long sendFromDate) {
		this.sendFromDate = sendFromDate;
	}

	public Long getSendToDate() {
		return sendToDate;
	}

	public void setSendToDate(Long sendToDate) {
		this.sendToDate = sendToDate;
	}

	public Long getAcceptFromDate() {
		return acceptFromDate;
	}

	public void setAcceptFromDate(Long acceptFromDate) {
		this.acceptFromDate = acceptFromDate;
	}

	public Long getAcceptToDate() {
		return acceptToDate;
	}

	public void setAcceptToDate(Long acceptToDate) {
		this.acceptToDate = acceptToDate;
	}

}
