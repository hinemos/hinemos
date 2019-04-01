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

import com.clustercontrol.repository.bean.NodeConfigSettingConstant;

/**
 * The persistent class for the cc_node_variable_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_node_variable_history_detail", schema="log")
@Cacheable(false)
public class NodeVariableHistoryDetail implements Serializable, NodeHistoryDetail {

	/** メンバ変数 */
	private NodeVariableHistoryDetailPK id;
	private static final long serialVersionUID = 1L;
	private Long regDateTo = NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE;
	private String nodeVariableValue = "";
	private String regUser = "";

	public NodeVariableHistoryDetail() {
	}

	public NodeVariableHistoryDetail(String facilityId, String nodeVariableName, Long regDate) {
		this(new NodeVariableHistoryDetailPK(facilityId, nodeVariableName, regDate));
	}
	
	public NodeVariableHistoryDetail(NodeVariableHistoryDetailPK id) {
		this.id = id;
	}

	
	@EmbeddedId
	@XmlTransient
	public NodeVariableHistoryDetailPK getId() {
		if (id == null) {
			id = new NodeVariableHistoryDetailPK();
		}
		return id;
	}
	public void setId(NodeVariableHistoryDetailPK id) {
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

	@XmlTransient
	@Transient
	public String getNodeVariableName() {
		return getId().getNodeVariableName();
	}
	public void setNodeVariableName(String nodeVariableName) {
		getId().setNodeVariableName(nodeVariableName);
	}

	@XmlTransient
	@Transient
	public Long getRegDate() {
		return getId().getRegDate();
	}
	public void setRegDate(Long regDate) {
		getId().setRegDate(regDate);
	}

	@Column(name = "reg_date_to")
	public Long getRegDateTo() {
		return regDateTo;
	}

	public void setRegDateTo(Long regDateTo) {
		this.regDateTo = regDateTo;
	}

	@Column(name="node_variable_value")
	public String getNodeVariableValue() {
		return nodeVariableValue;
	}
	public void setNodeVariableValue(String nodeVariableValue) {
		this.nodeVariableValue = nodeVariableValue;
	}

	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
}