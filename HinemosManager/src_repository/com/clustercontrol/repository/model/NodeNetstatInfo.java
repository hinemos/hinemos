/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.model;

import java.io.Serializable;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.util.HinemosTime;

/**
 * The persistent class for the cc_cfg_node_netstat database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_netstat", schema="setting")
@Cacheable(false)
public class NodeNetstatInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private NodeNetstatInfoPK id;
	private String status = "";
	private Long regDate = HinemosTime.currentTimeMillis();
	private String regUser = "";
	private Long updateDate = HinemosTime.currentTimeMillis();
	private String updateUser = "";
	private Boolean searchTarget = Boolean.FALSE;

	public NodeNetstatInfo() {
	}
	
	public NodeNetstatInfo(
			String facilityId, 
			String protocol, 
			String localIpAddress, 
			String localPort,
			String foreignIpAddress, 
			String foreignPort,
			String processName,
			Integer pid) {
		this(new NodeNetstatInfoPK(
				facilityId, protocol, localIpAddress, localPort, foreignIpAddress, foreignPort,
				processName, pid));
	}
	public NodeNetstatInfo(NodeNetstatInfoPK id) {
		this.id = id;
	}

	@XmlTransient
	@EmbeddedId
	public NodeNetstatInfoPK getId() {
		if (id == null)
			id = new NodeNetstatInfoPK();
		return id;
	}
	public void setId(NodeNetstatInfoPK id) {
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
	public String getProtocol() {
		return getId().getProtocol();
	}
	public void setProtocol(String protocol) {
		getId().setProtocol(protocol);
	}

	@Transient
	public String getLocalIpAddress() {
		return getId().getLocalIpAddress();
	}
	public void setLocalIpAddress(String localIpAddress) {
		getId().setLocalIpAddress(localIpAddress);
	}

	@Transient
	public String getLocalPort() {
		return getId().getLocalPort();
	}
	public void setLocalPort(String localPort) {
		getId().setLocalPort(localPort);
	}
	
	@Transient
	public String getForeignIpAddress() {
		return getId().getForeignIpAddress();
	}
	public void setForeignIpAddress(String foreignIpAddress) {
		getId().setForeignIpAddress(foreignIpAddress);
	}

	@Transient
	public String getForeignPort() {
		return getId().getForeignPort();
	}
	public void setForeignPort(String foreignPort) {
		getId().setForeignPort(foreignPort);
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

	@Column(name="status")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
	public NodeNetstatInfo clone() {
		try {
			NodeNetstatInfo cloneInfo = (NodeNetstatInfo)super.clone();
			cloneInfo.id = this.id;
			cloneInfo.status = this.status;
			cloneInfo.regDate = this.regDate;
			cloneInfo.regUser = this.regUser;
			cloneInfo.updateDate = this.updateDate;
			cloneInfo.updateUser = this.updateUser;
			cloneInfo.searchTarget = this.searchTarget;
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.getMessage());
		}
	}

	@Override
	public String toString() {
		return "NodeNetstatInfo ["
				+ "id=" + id
				+ ", status=" + status
				+ ", regDate=" + regDate
				+ ", regUser=" + regUser
				+ ", updateDate=" + updateDate
				+ ", updateUser=" + updateUser
				+ ", searchTarget=" + searchTarget
				+ "]";
	}
}