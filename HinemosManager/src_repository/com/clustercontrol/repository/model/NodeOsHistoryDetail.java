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
 * The persistent class for the cc_node_os_history_detail database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_node_os_history_detail", schema="log")
@Cacheable(false)
public class NodeOsHistoryDetail implements Serializable, NodeHistoryDetail {

	/** メンバ変数 */
	private NodeOsHistoryDetailPK id;
	private static final long serialVersionUID = 1L;
	private Long regDateTo = NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE;
	private String osName = "";
	private String osRelease = "";
	private String osVersion = "";
	private String characterSet = "";
	private Long startupDateTime = 0L;
	private String regUser = "";

	public NodeOsHistoryDetail() {
	}

	public NodeOsHistoryDetail(	String facilityId, Long regDate) {
		this(new NodeOsHistoryDetailPK(facilityId, regDate));
	}
	
	public NodeOsHistoryDetail(NodeOsHistoryDetailPK id) {
		this.id = id;
	}

	
	@EmbeddedId
	@XmlTransient
	public NodeOsHistoryDetailPK getId() {
		if (id == null) {
			id = new NodeOsHistoryDetailPK();
		}
		return id;
	}
	public void setId(NodeOsHistoryDetailPK id) {
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

	@Column(name="os_name")
	public String getOsName() {
		return osName;
	}
	public void setOsName(String osName) {
		this.osName = osName;
	}

	@Column(name="os_release")
	public String getOsRelease() {
		return osRelease;
	}
	public void setOsRelease(String osRelease) {
		this.osRelease = osRelease;
	}

	@Column(name="os_version")
	public String getOsVersion() {
		return osVersion;
	}
	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	@Column(name="character_set")
	public String getCharacterSet() {
		return characterSet;
	}
	public void setCharacterSet(String characterSet) {
		this.characterSet = characterSet;
	}

	@Column(name="startup_date_time")
	public Long getStartupDateTime() {
		return startupDateTime;
	}
	public void setStartupDateTime(Long startupDateTime) {
		this.startupDateTime = startupDateTime;
	}

	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
}