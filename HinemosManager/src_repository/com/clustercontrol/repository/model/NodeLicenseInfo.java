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

import com.clustercontrol.util.HinemosTime;

/**
 * The persistent class for the cc_cfg_node_license database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_license", schema="setting")
@Cacheable(false)
public class NodeLicenseInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private NodeLicenseInfoPK id;
	private String vendor = "";
	private String vendorContact = "";
	private String serialNumber = "";
	private Integer count = 0;
	private Long expirationDate = 0L;
	private Long regDate = HinemosTime.currentTimeMillis();
	private String regUser = "";
	private Long updateDate = HinemosTime.currentTimeMillis();
	private String updateUser = "";
	private Boolean searchTarget = Boolean.FALSE;

	public NodeLicenseInfo() {
	}
	
	public NodeLicenseInfo(String facilityId, String productName) {
		this(new NodeLicenseInfoPK(facilityId, productName));
	}
	public NodeLicenseInfo(NodeLicenseInfoPK id) {
		this.id = id;
	}

	@XmlTransient
	@EmbeddedId
	public NodeLicenseInfoPK getId() {
		if (id == null)
			id = new NodeLicenseInfoPK();
		return id;
	}
	public void setId(NodeLicenseInfoPK id) {
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
	public String getProductName() {
		return getId().getProductName();
	}
	public void setProductName(String productName) {
		getId().setProductName(productName);
	}

	@Column(name="vendor")
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	@Column(name="vendor_contact")
	public String getVendorContact() {
		return vendorContact;
	}
	public void setVendorContact(String vendorContact) {
		this.vendorContact = vendorContact;
	}

	@Column(name="serial_number")
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	@Column(name="count")
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}

	@Column(name="expiration_date")
	public Long getExpirationDate() {
		return expirationDate;
	}
	public void setExpirationDate(Long expirationDate) {
		this.expirationDate = expirationDate;
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
	public NodeLicenseInfo clone() {
		try {
			NodeLicenseInfo cloneInfo = (NodeLicenseInfo)super.clone();
			cloneInfo.id = this.id;
			cloneInfo.vendor = this.vendor;
			cloneInfo.vendorContact = this.vendorContact;
			cloneInfo.serialNumber = this.serialNumber;
			cloneInfo.count = this.count;
			cloneInfo.expirationDate = this.expirationDate;
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
		return "NodeLicenseInfo ["
				+ "id=" + id
				+ ", vendor=" + vendor
				+ ", vendorContact=" + vendorContact
				+ ", serialNumber=" + serialNumber
				+ ", count=" + count
				+ ", expirationDate=" + expirationDate
				+ ", regDate=" + regDate
				+ ", regUser=" + regUser
				+ ", updateDate=" + updateDate
				+ ", updateUser=" + updateUser
				+ ", searchTarget=" + searchTarget
				+ "]";
	}
}