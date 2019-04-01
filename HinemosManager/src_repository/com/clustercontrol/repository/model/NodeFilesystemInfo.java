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
 * The persistent class for the cc_cfg_node_filesystem database table.
 * 
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_filesystem", schema="setting")
@Cacheable(false)
public class NodeFilesystemInfo extends NodeDeviceInfo {
	private static final long serialVersionUID = 1L;
	private String deviceFilesystemType			= "";
	private Long regDate = HinemosTime.currentTimeMillis();
	private String regUser = "";
	private Long updateDate = HinemosTime.currentTimeMillis();
	private String updateUser = "";
	private Boolean searchTarget = Boolean.FALSE;

	public NodeFilesystemInfo() {
		super();
	}

	public NodeFilesystemInfo(
			String facilityId,
			Integer deviceIndex,
			String deviceType,
			String deviceName) {
		super(facilityId,
				deviceIndex,
				deviceType,
				deviceName);
	}

	public NodeFilesystemInfo(NodeDeviceInfoPK pk) {
		super(pk);
	}
	
	@Column(name="device_filesystem_type")
	public String getFilesystemType() {
		return this.deviceFilesystemType;
	}

	public void setFilesystemType(String deviceFilesystemType) {
		this.deviceFilesystemType = deviceFilesystemType;
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
	public NodeFilesystemInfo clone() {
		NodeFilesystemInfo cloneInfo = (NodeFilesystemInfo)super.clone();
		cloneInfo.deviceFilesystemType = this.deviceFilesystemType;
		cloneInfo.regDate = this.regDate;
		cloneInfo.regUser = this.regUser;
		cloneInfo.updateDate = this.updateDate;
		cloneInfo.updateUser = this.updateUser;
		cloneInfo.searchTarget = this.searchTarget;
		return cloneInfo;
	}

	@Override
	public String toString() {
		return "NodeFilesystemInfo ["
				+ super.toString()
				+ ", deviceFilesystemType=" + deviceFilesystemType
				+ ", regDate=" + regDate
				+ ", regUser=" + regUser
				+ ", updateDate=" + updateDate
				+ ", updateUser=" + updateUser
				+ ", searchTarget=" + searchTarget
				+ "]";
	}

}