/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
 * The persistent class for the cc_cfg_node_variable database table.
 * 
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_variable", schema="setting")
@Cacheable(false)
public class NodeVariableInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private NodeVariableInfoPK id;
	private String nodeVariableValue			= "";
	private Long regDate = HinemosTime.currentTimeMillis();
	private String regUser = "";
	private Long updateDate = HinemosTime.currentTimeMillis();
	private String updateUser = "";
	private Boolean searchTarget = Boolean.FALSE;

	public NodeVariableInfo() {
	}

	public NodeVariableInfo(String facilityId, String nodeVariableName) {
		this(new NodeVariableInfoPK(facilityId, nodeVariableName));
	}

	public NodeVariableInfo(NodeVariableInfoPK id) {
		this.id = id;
	}
	
	@EmbeddedId
	@XmlTransient
	public NodeVariableInfoPK getId() {
		if (id == null)
			id = new NodeVariableInfoPK();
		return this.id;
	}
	public void setId(NodeVariableInfoPK id) {
		this.id = id;
	}
	
	@XmlTransient
	@Transient
	public String getFacilityId() {
		return getId().getFacilityId();
	}
	public void setFacilityId(String facilityId) {
		this.getId().setFacilityId(facilityId);
	}

	@Transient
	public String getNodeVariableName() {
		return getId().getNodeVariableName();
	}
	public void setNodeVariableName(String nodeVariableName) {
		this.getId().setNodeVariableName(nodeVariableName);
	}

	@Column(name="node_variable_value")
	public String getNodeVariableValue() {
		return this.nodeVariableValue;
	}

	public void setNodeVariableValue(String nodeVariableValue) {
		this.nodeVariableValue = nodeVariableValue;
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
	public NodeVariableInfo clone() {
		try {
			NodeVariableInfo cloneInfo = (NodeVariableInfo)super.clone();
			cloneInfo.id = this.id;
			cloneInfo.nodeVariableValue = this.nodeVariableValue;
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
		return "NodeVariableInfo ["
				+ "id=" + id 
				+ ", nodeVariableValue=" + nodeVariableValue 
				+ ", regDate=" + regDate
				+ ", regUser=" + regUser 
				+ ", updateDate=" + updateDate 
				+ ", updateUser=" + updateUser
				+ ", searchTarget=" + searchTarget 
				+ "]";
	}
}