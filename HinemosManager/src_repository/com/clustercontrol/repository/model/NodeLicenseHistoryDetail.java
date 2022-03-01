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
 * The persistent class for the cc_cfg_node_license database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name = "cc_node_license_history_detail", schema = "log")
@Cacheable(false)
public class NodeLicenseHistoryDetail implements Serializable, NodeHistoryDetail {
	private static final long serialVersionUID = 1L;
	private NodeProductHistoryDetailPK id;
	private Long regDateTo = NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE;
	private String vendor = "";
	private String vendorContact = "";
	private String serialNumber = "";
	private Integer count = 0;
	private Long expirationDate = 0L;
	private String regUser = "";

	/**
	 * デフォルト値生成コンストラクタ.
	 */
	public NodeLicenseHistoryDetail() {
	}

	/**
	 * キー値指定コンストラクタ.
	 */
	public NodeLicenseHistoryDetail(String facilityId, String productName, Long regDate) {
		this(new NodeProductHistoryDetailPK(facilityId, productName, regDate));
	}

	/**
	 * キーオブジェクト指定コンストラクタ.
	 */
	public NodeLicenseHistoryDetail(NodeProductHistoryDetailPK id) {
		this.id = id;
	}

	@XmlTransient
	@EmbeddedId
	public NodeProductHistoryDetailPK getId() {
		if (id == null)
			id = new NodeProductHistoryDetailPK();
		return id;
	}

	public void setId(NodeProductHistoryDetailPK id) {
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

	@Column(name = "vendor")
	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	@Column(name = "vendor_contact")
	public String getVendorContact() {
		return vendorContact;
	}

	public void setVendorContact(String vendorContact) {
		this.vendorContact = vendorContact;
	}

	@Column(name = "serial_number")
	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	@Column(name = "count")
	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	@Column(name = "expiration_date")
	public Long getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Long expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Column(name = "reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
}