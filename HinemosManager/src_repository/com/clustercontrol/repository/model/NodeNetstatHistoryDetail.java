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

import com.clustercontrol.repository.bean.NodeConfigSettingConstant;

/**
 * The persistent class for the cc_node_netstat_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name = "cc_node_netstat_history_detail", schema = "log")
@Cacheable(false)
public class NodeNetstatHistoryDetail implements Serializable, NodeHistoryDetail {
	private static final long serialVersionUID = 1L;
	private NodeNetstatHistoryDetailPK id;
	private Long regDateTo = NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE;
	private String status = "";
	private String regUser = "";

	/**
	 * デフォルト値生成コンストラクタ.
	 */
	public NodeNetstatHistoryDetail() {
	}

	/**
	 * キー値指定コンストラクタ.
	 */
	public NodeNetstatHistoryDetail(
			String facilityId, 
			String protocol, 
			String localIpAddress, 
			String localPort, 
			String foreignIpAddress, 
			String foreignPort,
			String processName, 
			Integer pid,
			Long regDate) {
		this(new NodeNetstatHistoryDetailPK(
				facilityId, protocol, localIpAddress, localPort, foreignIpAddress, foreignPort, 
				processName, pid, regDate));
	}

	/**
	 * キーオブジェクト指定コンストラクタ.
	 */
	public NodeNetstatHistoryDetail(NodeNetstatHistoryDetailPK id) {
		this.id = id;
	}

	@XmlTransient
	@EmbeddedId
	public NodeNetstatHistoryDetailPK getId() {
		if (id == null)
			id = new NodeNetstatHistoryDetailPK();
		return id;
	}
	public void setId(NodeNetstatHistoryDetailPK id) {
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

	@Column(name = "status")
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Column(name = "reg_user")
	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
}