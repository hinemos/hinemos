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
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.util.HinemosTime;


/**
 * The persistent class for the cc_cfg_node_cpu database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_os", schema="setting")
@Cacheable(false)
public class NodeOsInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private String facilityId;
	private String osName					= "";
	private String osRelease				= "";
	private String osVersion				= "";
	private String characterSet				= "";
	private Long startupDateTime = 0L;
	private Long regDate = HinemosTime.currentTimeMillis();
	private String regUser = "";
	private Long updateDate = HinemosTime.currentTimeMillis();
	private String updateUser = "";
	private Boolean searchTarget = Boolean.FALSE;

	public NodeOsInfo() {
		super();
	}

	public NodeOsInfo(String facilityId) {
		this.setFacilityId(facilityId);
	}

	@Id
	@Column(name="facility_id")
	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	@Column(name="os_name")
	public String getOsName() {
		return this.osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}


	@Column(name="os_release")
	public String getOsRelease() {
		return this.osRelease;
	}

	public void setOsRelease(String osRelease) {
		this.osRelease = osRelease;
	}

	@Column(name="os_version")
	public String getOsVersion() {
		return this.osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	@Column(name="character_set")
	public String getCharacterSet() {
		return this.characterSet;
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
	public NodeOsInfo clone() {
		try {
			NodeOsInfo cloneInfo = (NodeOsInfo)super.clone();
			cloneInfo.facilityId = this.facilityId;
			cloneInfo.osName = this.osName;
			cloneInfo.osRelease = this.osRelease;
			cloneInfo.osVersion = this.osVersion;
			cloneInfo.characterSet = this.characterSet;
			cloneInfo.startupDateTime = this.startupDateTime;
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
		return "NodeOsInfo ["
				+ "facilityId=" + facilityId 
				+ ", osName=" + osName 
				+ ", osRelease=" + osRelease
				+ ", osVersion=" + osVersion
				+ ", characterSet=" + characterSet 
				+ ", startupDateTime=" + startupDateTime
				+ ", regDate=" + regDate 
				+ ", regUser="+ regUser 
				+ ", updateDate=" + updateDate 
				+ ", updateUser=" + updateUser 
				+ ", searchTarget=" + searchTarget
				+ "]";
	}
}