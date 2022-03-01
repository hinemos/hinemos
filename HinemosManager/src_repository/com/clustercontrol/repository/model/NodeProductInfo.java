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
 * The persistent class for the cc_cfg_node_product database table.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
@Entity
@Table(name="cc_cfg_node_product", schema="setting")
@Cacheable(false)
public class NodeProductInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private NodeProductInfoPK id;
	private String version = "";
	private String path = "";
	private Long regDate = HinemosTime.currentTimeMillis();
	private String regUser = "";
	private Long updateDate = HinemosTime.currentTimeMillis();
	private String updateUser = "";
	private Boolean searchTarget = Boolean.FALSE;

	public NodeProductInfo() {
	}
	
	public NodeProductInfo(String facilityId, String productName) {
		this(new NodeProductInfoPK(facilityId, productName));
	}
	public NodeProductInfo(NodeProductInfoPK id) {
		this.id = id;
	}

	@XmlTransient
	@EmbeddedId
	public NodeProductInfoPK getId() {
		if (id == null)
			id = new NodeProductInfoPK();
		return id;
	}
	public void setId(NodeProductInfoPK id) {
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

	@Column(name="version")
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	@Column(name="path")
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
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
	public NodeProductInfo clone() {
		try {
			NodeProductInfo cloneInfo = (NodeProductInfo)super.clone();
			cloneInfo.id = this.id;
			cloneInfo.version = this.version;
			cloneInfo.path = this.path;
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
		return "NodeProductInfo ["
				+ "id=" + id 
				+ ", version=" + version 
				+ ", path=" + path 
				+ ", regDate=" + regDate
				+ ", regUser=" + regUser 
				+ ", updateDate=" + updateDate 
				+ ", updateUser=" + updateUser
				+ ", searchTarget=" + searchTarget 
				+ "]";
	}
}