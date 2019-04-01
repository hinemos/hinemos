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
 * The persistent class for the cc_cfg_node_package database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name = "cc_node_package_history_detail", schema = "log")
@Cacheable(false)
public class NodePackageHistoryDetail implements Serializable, NodeHistoryDetail {
	private static final long serialVersionUID = 1L;
	private NodePackageHistoryDetailPK id;
	private Long regDateTo = NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE;
	private String packageName = "";
	private String version = "";
	private String release = "";
	private Long installDate = 0L;
	private String vendor = "";
	private String architecture = "";
	private String regUser = "";

	/**
	 * デフォルト値生成コンストラクタ.
	 */
	public NodePackageHistoryDetail() {
	}

	/**
	 * キー値指定コンストラクタ.
	 */
	public NodePackageHistoryDetail(String facilityId, String packageId, Long regDate) {
		this(new NodePackageHistoryDetailPK(facilityId, packageId, regDate));
	}

	/**
	 * キーオブジェクト指定コンストラクタ.
	 */
	public NodePackageHistoryDetail(NodePackageHistoryDetailPK id) {
		this.id = id;
	}

	@XmlTransient
	@EmbeddedId
	public NodePackageHistoryDetailPK getId() {
		if (id == null)
			id = new NodePackageHistoryDetailPK();
		return id;
	}

	public void setId(NodePackageHistoryDetailPK id) {
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

	@Column(name = "package_name")
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	@Column(name = "version")
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Column(name = "release")
	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	@Column(name = "install_date")
	public Long getInstallDate() {
		return installDate;
	}

	public void setInstallDate(Long installDate) {
		this.installDate = installDate;
	}

	@Column(name = "vendor")
	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	@Column(name = "architecture")
	public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	@Column(name = "reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
}