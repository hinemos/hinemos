/*

Copyright (C) 2010 NTT DATA Corporation

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

/**
 * 
 * ジョブ履歴のフィルタ
 *
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobHistoryFilter implements Serializable{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -8999967944715277063L;

	private Long startFromDate = null;
	private Long startToDate = null;
	private Long endFromDate = null;
	private Long endToDate = null;
	private String jobId = null;
	private Integer status = null;
	private Integer endStatus = null;
	private Integer triggerType = null;
	private String triggerInfo = null;
	private String ownerRoleId = null;

	public JobHistoryFilter(Long startFromDate, Long startToDate,
			Long endFromDate, Long endToDate, String jobId, Integer status, Integer endStatus,
			Integer triggerType, String triggerInfo, String ownerRoleId) {
		super();
		this.startFromDate = startFromDate;
		this.startToDate = startToDate;
		this.endFromDate = endFromDate;
		this.endToDate = endToDate;
		this.jobId = jobId;
		this.status = status;
		this.endStatus = endStatus;
		this.triggerType = triggerType;
		this.triggerInfo = triggerInfo;
		this.ownerRoleId = ownerRoleId;
	}

	public JobHistoryFilter() {}

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

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getEndStatus() {
		return endStatus;
	}

	public void setEndStatus(Integer endStatus) {
		this.endStatus = endStatus;
	}

	public Integer getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(Integer triggerType) {
		this.triggerType = triggerType;
	}

	public String getTriggerInfo() {
		return triggerInfo;
	}

	public void setTriggerInfo(String triggerInfo) {
		this.triggerInfo = triggerInfo;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

}
