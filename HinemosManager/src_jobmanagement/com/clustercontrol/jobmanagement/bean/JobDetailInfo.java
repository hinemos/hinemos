/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobDetailInfo implements Serializable {

	private static final long serialVersionUID = -4404769115576418195L;

	/** 実行状態(履歴で利用) */
	private Integer m_status;

	/** 終了状態 */
	private Integer m_endStatus;

	/** 終了値 */
	private Integer m_endValue;

	/** ファシリティID */
	private String m_facilityId;

	/** スコープ */
	private String m_scope;

	/** 時刻 */
	private Long m_waitRuleTime;

	/** セッション開始時の時間（分） */
	private Integer m_startMinute;

	/** 開始時刻 */
	private Long m_startDate;

	/** 終了時刻 */
	private Long m_endDate;

	public Integer getStatus() {
		return m_status;
	}

	public void setStatus(Integer status) {
		this.m_status = status;
	}

	public Integer getEndStatus() {
		return m_endStatus;
	}

	public void setEndStatus(Integer endStatus) {
		this.m_endStatus = endStatus;
	}

	public Integer getEndValue() {
		return m_endValue;
	}

	public void setEndValue(Integer endValue) {
		this.m_endValue = endValue;
	}

	public String getFacilityId() {
		return m_facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.m_facilityId = facilityId;
	}

	public String getScope() {
		return m_scope;
	}

	public void setScope(String scope) {
		this.m_scope = scope;
	}

	public Long getWaitRuleTime() {
		return m_waitRuleTime;
	}

	public void setWaitRuleTime(Long waitRuleTime) {
		this.m_waitRuleTime = waitRuleTime;
	}

	public Integer getStartMinute() {
		return m_startMinute;
	}

	public void setStartMinute(Integer startMinute) {
		this.m_startMinute = startMinute;
	}

	public Long getStartDate() {
		return m_startDate;
	}

	public void setStartDate(Long startDate) {
		this.m_startDate = startDate;
	}

	public Long getEndDate() {
		return m_endDate;
	}

	public void setEndDate(Long endDate) {
		this.m_endDate = endDate;
	}

}
