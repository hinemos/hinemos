/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * The persistent class for the cc_infra_management_param_info database table.
 * 
 */
@XmlType(namespace = "http://infra.ws.clustercontrol.com")
@Entity
@Table(name="cc_infra_management_param_info", schema="setting")
@Cacheable(true)
public class InfraManagementParamInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private InfraManagementParamInfoPK id;
	private Boolean passwordFlg;
	private String description;
	private String value;
	private InfraManagementInfo infraManagementInfo;

	public InfraManagementParamInfo() {
	}

	public InfraManagementParamInfo(InfraManagementParamInfoPK pk) {
		this.setId(pk);
	}

	public InfraManagementParamInfo(String managementId, String paramId) {
		this(new InfraManagementParamInfoPK(managementId, paramId));
	}

	@XmlTransient
	@EmbeddedId
	public InfraManagementParamInfoPK getId() {
		if (id == null) {
			id = new InfraManagementParamInfoPK();
		}
		return this.id;
	}

	public void setId(InfraManagementParamInfoPK id) {
		this.id = id;
	}

	@Transient
	public String getManagementId() {
		return getId().getManagementId();
	}
	public void setManagementId(String managementId) {
		getId().setManagementId(managementId);
	}

	@Transient
	public String getParamId() {
		return getId().getParamId();
	}
	public void setParamId(String paramId) {
		getId().setParamId(paramId);
	}

	@Column(name="password_flg")
	public Boolean getPasswordFlg() {
		return passwordFlg;
	}
	public void setPasswordFlg(Boolean passwordFlg) {
		this.passwordFlg = passwordFlg;
	}

	@Column(name="description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name="value")
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	//bi-directional many-to-one association to InfraManagementInfo
	@XmlTransient
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="management_id", insertable=false, updatable=false)
	public InfraManagementInfo getInfraManagementInfo() {
		return this.infraManagementInfo;
	}

	@Deprecated
	public void setInfraManagementInfo(InfraManagementInfo infraManagementInfo) {
		this.infraManagementInfo = infraManagementInfo;
	}

	/**
	 * InfraManagementInfoオブジェクト参照設定<BR>
	 * 
	 * InfraManagementInfo設定時はSetterに代わりこちらを使用すること。
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToInfraManagementInfo(InfraManagementInfo infraManagementInfo) {
		this.setInfraManagementInfo(infraManagementInfo);
		if (infraManagementInfo != null) {
			List<InfraManagementParamInfo> list = infraManagementInfo.getInfraManagementParamList();
			if (list == null) {
				list = new ArrayList<InfraManagementParamInfo>();
			} else {
				for(InfraManagementParamInfo entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			infraManagementInfo.setInfraManagementParamList(list);
		}
	}

	/**
	 * 削除前処理<BR>
	 * 
	 * JPAの仕様(JSR 220)では、データ更新に伴うrelationshipの管理はユーザに委ねられており、
	 * INSERTやDELETE時に、そのオブジェクトに対する参照をメンテナンスする処理を実装する。
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developer’s responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// InfraManagementInfo
		if (this.infraManagementInfo != null) {
			List<InfraManagementParamInfo> list = this.infraManagementInfo.getInfraManagementParamList();
			if (list != null) {
				Iterator<InfraManagementParamInfo> iter = list.iterator();
				while(iter.hasNext()) {
					InfraManagementParamInfo entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}

}