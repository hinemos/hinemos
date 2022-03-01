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
 * The persistent class for the cc_cfg_node_package database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_package", schema="setting")
@Cacheable(false)
public class NodePackageInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private NodePackageInfoPK id;
	private String packageName = "";
	private String version = "";
	private String release = "";
	private Long installDate = 0L;
	private String vendor = "";
	private String architecture = "";
	private Long regDate = HinemosTime.currentTimeMillis();
	private String regUser = "";
	private Long updateDate = HinemosTime.currentTimeMillis();
	private String updateUser = "";
	private Boolean searchTarget = Boolean.FALSE;

	public NodePackageInfo() {
	}
	
	public NodePackageInfo(String facilityId, String packageId) {
		this(new NodePackageInfoPK(facilityId, packageId));
	}
	public NodePackageInfo(NodePackageInfoPK id) {
		this.id = id;
	}

	@XmlTransient
	@EmbeddedId
	public NodePackageInfoPK getId() {
		if (id == null)
			id = new NodePackageInfoPK();
		return id;
	}
	public void setId(NodePackageInfoPK id) {
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
	public String getPackageId() {
		return getId().getPackageId();
	}
	public void setPackageId(String packageId) {
		getId().setPackageId(packageId);
	}

	@Column(name="package_name")
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Column(name="version")
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	@Column(name="release")
	public String getRelease() {
		return release;
	}
	public void setRelease(String release) {
		this.release = release;
	}

	@Column(name="install_date")
	public Long getInstallDate() {
		return installDate;
	}
	public void setInstallDate(Long installDate) {
		this.installDate = installDate;
	}

	@Column(name="vendor")
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	@Column(name="architecture")
	public String getArchitecture() {
		return architecture;
	}
	public void setArchitecture(String architecture) {
		this.architecture = architecture;
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
	public NodePackageInfo clone() {
		try {
			NodePackageInfo cloneInfo = (NodePackageInfo)super.clone();
			cloneInfo.id = this.id;
			cloneInfo.packageName = this.packageName;
			cloneInfo.version = this.version;
			cloneInfo.release = this.release;
			cloneInfo.installDate = this.installDate;
			cloneInfo.vendor = this.vendor;
			cloneInfo.architecture = this.architecture;
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
		return "NodePackageInfo ["
				+ "id=" + id
				+ ", packageName=" + packageName 
				+ ", version=" + version 
				+ ", release=" + release 
				+ ", installDate=" + installDate 
				+ ", vendor=" + vendor 
				+ ", architecture=" + architecture
				+ ", regDate=" + regDate 
				+ ", regUser=" + regUser
				+ ", updateDate=" + updateDate
				+ ", updateUser=" + updateUser
				+ ", searchTarget=" + searchTarget
				+ "]";
	}
}