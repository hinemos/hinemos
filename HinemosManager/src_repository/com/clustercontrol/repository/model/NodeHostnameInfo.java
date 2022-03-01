/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
 * The persistent class for the cc_cfg_node_hostname database table.
 * 
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_hostname", schema="setting")
@Cacheable(false)
public class NodeHostnameInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private NodeHostnameInfoPK id;
	private Long regDate = HinemosTime.currentTimeMillis();
	private String regUser = "";
	private Boolean searchTarget = Boolean.FALSE;

	public NodeHostnameInfo() {
	}
	
	public NodeHostnameInfo(String facilityId, String hostname) {
		this(new NodeHostnameInfoPK(facilityId, hostname));
	}
	public NodeHostnameInfo(NodeHostnameInfoPK id) {
		this.id = id;
	}

	@XmlTransient
	@EmbeddedId
	public NodeHostnameInfoPK getId() {
		if (id == null)
			id = new NodeHostnameInfoPK();
		return id;
	}
	public void setId(NodeHostnameInfoPK id) {
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
	public String getHostname() {
		return getId().getHostname();
	}
	public void setHostname(String hostname) {
		getId().setHostname(hostname);
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
	public NodeHostnameInfo clone() {
		try {
			NodeHostnameInfo cloneInfo = (NodeHostnameInfo)super.clone();
			cloneInfo.id = this.id;
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
		return "NodeHostnameInfo ["
				+ "id=" + id 
				+ ", regDate=" + regDate 
				+ ", regUser=" + regUser 
				+ ", searchTarget=" + searchTarget
				+ "]";
	}

}