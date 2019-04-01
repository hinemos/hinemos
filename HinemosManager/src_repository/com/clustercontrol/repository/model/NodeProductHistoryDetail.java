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
 * The persistent class for the cc_cfg_node_product database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name = "cc_node_product_history_detail", schema = "log")
@Cacheable(false)
public class NodeProductHistoryDetail implements Serializable, NodeHistoryDetail {
	private static final long serialVersionUID = 1L;
	private NodeProductHistoryDetailPK id;
	private Long regDateTo = NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE;
	private String version = "";
	private String path = "";
	private String regUser = "";

	/**
	 * デフォルト値生成コンストラクタ.
	 */
	public NodeProductHistoryDetail() {
	}

	/**
	 * キー値指定コンストラクタ.
	 */
	public NodeProductHistoryDetail(String facilityId, String productName, Long regDate) {
		this(new NodeProductHistoryDetailPK(facilityId, productName, regDate));
	}

	/**
	 * キーオブジェクト指定コンストラクタ.
	 */
	public NodeProductHistoryDetail(NodeProductHistoryDetailPK id) {
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

	@Column(name = "version")
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Column(name = "path")
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Column(name = "reg_user")
	public String getRegUser() {
		return this.regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
}