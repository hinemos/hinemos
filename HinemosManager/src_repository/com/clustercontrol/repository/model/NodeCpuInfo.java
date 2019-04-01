/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.util.HinemosTime;

/**
 * The persistent class for the cc_cfg_node_cpu database table.
 * 
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_cpu", schema="setting")
@Cacheable(false)
public class NodeCpuInfo extends NodeDeviceInfo {
	private static final long serialVersionUID = 1L;
	private Integer coreCount = 0;
	private Integer threadCount = 0;
	private Integer clockCount = 0;
	private Long regDate = HinemosTime.currentTimeMillis();
	private String regUser = "";
	private Long updateDate = HinemosTime.currentTimeMillis();
	private String updateUser = "";
	private Boolean searchTarget = Boolean.FALSE;

	public NodeCpuInfo() {
		super();
	}

	public NodeCpuInfo(
			String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName) {
		super(facilityId,
				deviceIndex,
				deviceType,
				deviceName);
	}
	
	public NodeCpuInfo(NodeDeviceInfoPK pk) {
		super(pk);
	}

	@Column(name="core_count")
	public Integer getCoreCount() {
		return coreCount;
	}

	public void setCoreCount(Integer coreCount) {
		this.coreCount = coreCount;
	}

	@Column(name="thread_count")
	public Integer getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
	}

	@Column(name="clock_count")
	public Integer getClockCount() {
		return clockCount;
	}

	public void setClockCount(Integer clockCount) {
		this.clockCount = clockCount;
	}

	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}


	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	@Transient
	public Boolean getSearchTarget() {
		return this.searchTarget;
	}
	public void setSearchTarget(Boolean searchTarget) {
		this.searchTarget = searchTarget;
	}

	@Override
	public NodeCpuInfo clone() {
		NodeCpuInfo cloneInfo = (NodeCpuInfo)super.clone();
		cloneInfo.coreCount = this.coreCount;
		cloneInfo.threadCount = this.threadCount;
		cloneInfo.clockCount = this.clockCount;
		cloneInfo.regDate = this.regDate;
		cloneInfo.regUser = this.regUser;
		cloneInfo.updateDate = this.updateDate;
		cloneInfo.updateUser = this.updateUser;
		cloneInfo.searchTarget = this.searchTarget;
		return cloneInfo;
	}

	@Override
	public String toString() {
		return "NodeCpuInfo ["
				+ super.toString()
				+ ", coreCount=" + coreCount
				+ ", threadCount=" + threadCount
				+ ", clockCount=" + clockCount
				+ ", regDate=" + regDate
				+ ", regUser=" + regUser 
				+ ", updateDate=" + updateDate
				+ ", updateUser=" + updateUser
				+ ", searchTarget=" + searchTarget
				+ "]";
	}
	
}