/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.util.HinemosTime;

/**
 * The persistent class for the cc_cfg_node_process database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_process", schema="setting")
@Cacheable(false)
public class NodeProcessInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private NodeProcessInfoPK id;
	private String path = "";
	private String execUser = "";
	private Long startupDateTime = 0L;
	private Long regDate = HinemosTime.currentTimeMillis();
	private String regUser = "";
	private Boolean searchTarget = Boolean.FALSE;

	public NodeProcessInfo() {
	}
	
	public NodeProcessInfo(String facilityId, String processName, Integer pid) {
		this(new NodeProcessInfoPK(facilityId, processName, pid));
	}
	public NodeProcessInfo(NodeProcessInfoPK id) {
		this.id = id;
	}

	@XmlTransient
	@EmbeddedId
	public NodeProcessInfoPK getId() {
		if (id == null)
			id = new NodeProcessInfoPK();
		return id;
	}
	public void setId(NodeProcessInfoPK id) {
		this.id = id;
	}
	
	@XmlTransient
	@Transient
	public String getFacilityId() {
		return getId().getFacilityId();
	}
	public void setFacilityId(String facilityId) {
		getId().setFacilityId(facilityId);
	}

	@Transient
	public String getProcessName() {
		return getId().getProcessName();
	}
	public void setProcessName(String processName) {
		getId().setProcessName(processName);
	}

	@Transient
	public Integer getPid() {
		return getId().getPid();
	}
	public void setPid(Integer pid) {
		getId().setPid(pid);
	}

	@Column(name="path")
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	@Column(name="exec_user")
	public String getExecUser() {
		return execUser;
	}
	public void setExecUser(String execUser) {
		this.execUser = execUser;
	}

	@Column(name="startup_date_time")
	public Long getStartupDateTime() {
		return startupDateTime;
	}
	public void setStartupDateTime(Long startupDateTime) {
		this.startupDateTime = startupDateTime;
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

	@Transient
	public Boolean getSearchTarget() {
		return this.searchTarget;
	}
	public void setSearchTarget(Boolean searchTarget) {
		this.searchTarget = searchTarget;
	}
	@Override
	public NodeProcessInfo clone() {
		try {
			NodeProcessInfo cloneInfo = (NodeProcessInfo)super.clone();
			cloneInfo.id = this.id;
			cloneInfo.path = this.path;
			cloneInfo.execUser = this.execUser;
			cloneInfo.startupDateTime = this.startupDateTime;
			cloneInfo.regDate = this.regDate;
			cloneInfo.regUser = this.regUser;
			cloneInfo.searchTarget = this.searchTarget;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.getMessage());
		}
	}

	@Override
	public String toString() {
		return "NodeProcessInfo ["
				+ "id=" + id 
				+ ", path=" + path 
				+ ", execUser=" + execUser 
				+ ", startupDateTime=" + startupDateTime 
				+ ", regDate=" + regDate 
				+ ", regUser=" + regUser 
				+ ", searchTarget=" + searchTarget
				+ "]";
	}

}