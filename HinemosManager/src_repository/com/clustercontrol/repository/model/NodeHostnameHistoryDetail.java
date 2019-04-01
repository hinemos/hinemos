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
 * The persistent class for the cc_node_hostname_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_node_hostname_history_detail", schema="log")
@Cacheable(false)
public class NodeHostnameHistoryDetail implements Serializable, NodeHistoryDetail {

	/** メンバ変数 */
	private NodeHostnameHistoryDetailPK id;
	private static final long serialVersionUID = 1L;
	private Long regDateTo = NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE;
	private String hostnameItem = "";
	private String regUser = "";

	public NodeHostnameHistoryDetail() {
	}

	public NodeHostnameHistoryDetail(String facilityId, String hostname, Long regDate) {
		this(new NodeHostnameHistoryDetailPK(facilityId, hostname, regDate));
	}
	
	public NodeHostnameHistoryDetail(NodeHostnameHistoryDetailPK id) {
		this.id = id;
	}

	
	@EmbeddedId
	@XmlTransient
	public NodeHostnameHistoryDetailPK getId() {
		if (id == null) {
			id = new NodeHostnameHistoryDetailPK();
		}
		return id;
	}
	public void setId(NodeHostnameHistoryDetailPK id) {
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
	public String getHostname() {
		return getId().getHostname();
	}
	public void setHostname(String hostname) {
		getId().setHostname(hostname);
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

	@Column(name="hostname_item")
	public String getHostnameItem() {
		return hostnameItem;
	}
	public void setHostnameItem(String hostnameItem) {
		this.hostnameItem = hostnameItem;
	}

	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
}