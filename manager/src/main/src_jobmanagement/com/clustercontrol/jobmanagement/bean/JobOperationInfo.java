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
 * ジョブ[履歴]・ジョブ[ジョブ詳細]・ジョブ[ノード詳細]ビューの「開始、停止」の際に利用されるクラス。
 *
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobOperationInfo implements Serializable{

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -3620839595048616073L;
	private String sessionId = null;
	private String jobunitId = null;
	private String jobId = null;
	private String facilityId = null;
	private Integer control = null;
	private Integer endStatus = null;
	private Integer endValue = null;

	public JobOperationInfo() {}

	public JobOperationInfo(String sessionId, String jobunitId, String jobId,
			String facilityId, Integer control, Integer endStatus, Integer endValue) {
		super();
		this.sessionId = sessionId;
		this.jobunitId = jobunitId;
		this.jobId = jobId;
		this.facilityId = facilityId;
		this.control = control;
		this.endStatus = endStatus;
		this.endValue = endValue;
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
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	public Integer getControl() {
		return control;
	}
	public void setControl(Integer control) {
		this.control = control;
	}
	public Integer getEndStatus() {
		return endStatus;
	}
	public void setEndStatus(Integer endStatus) {
		this.endStatus = endStatus;
	}
	public Integer getEndValue() {
		return endValue;
	}
	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}
}
