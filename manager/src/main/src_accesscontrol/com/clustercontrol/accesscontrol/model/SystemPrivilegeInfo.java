/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;


/**
 * The persistent class for the cc_system_privilege database table.
 * 
 */
@Entity
@Table(name="cc_system_privilege", schema="setting")
@Cacheable(true)
public class SystemPrivilegeInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private SystemPrivilegeInfoPK id;
	private String editType;
	private List<RoleInfo> roleList;

	public SystemPrivilegeInfo() {
	}

	public SystemPrivilegeInfo(String systemFunction, String systemPrivilege) {
		this.setSystemFunction(systemFunction);
		this.setSystemPrivilege(systemPrivilege);
	}

	public SystemPrivilegeInfo(String systemFunction, SystemPrivilegeMode systemPrivilege) {
		this.setSystemFunction(systemFunction);
		this.setSystemPrivilege(systemPrivilege.name());
	}

	@XmlTransient
	@EmbeddedId
	public SystemPrivilegeInfoPK getId() {
		if (this.id == null)
			this.id = new SystemPrivilegeInfoPK();
		return this.id;
	}
	public void setId(SystemPrivilegeInfoPK id) {
		this.id = id;
	}

	@Transient
	public String getSystemFunction() {
		return getId().getSystemFunction();
	}
	public void setSystemFunction(String systemFunction) {
		getId().setSystemFunction(systemFunction);
	}

	@Transient
	public String getSystemPrivilege() {
		return getId().getSystemPrivilege();
	}
	public void setSystemPrivilege(String systemPrivilege) {
		getId().setSystemPrivilege(systemPrivilege);
	}

	@Column(name="edit_type")
	public String getEditType() {
		return this.editType;
	}
	public void setEditType(String editType) {
		this.editType = editType;
	}

	@XmlTransient
	//bi-directional many-to-many association to RoleInfo
	@ManyToMany(mappedBy="systemPrivilegeList", cascade={CascadeType.PERSIST, CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH})
	public List<RoleInfo> getRoleList() {
		return this.roleList;
	}
	public void setRoleList(List<RoleInfo> roleList) {
		this.roleList = roleList;
	}

	/**
	 * RoleInfoリレーション削除処理<BR>
	 * 
	 * RoleInfoに存在するSystemPrivilegeInfoを削除する。
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
	public void unchainSystemPrivilegeList() {

		if (this.roleList != null && this.roleList.size() > 0) {
			for(RoleInfo roleInfo : this.roleList) {
				List<SystemPrivilegeInfo> list = roleInfo.getSystemPrivilegeList();
				if (list != null) {
					Iterator<SystemPrivilegeInfo> iter = list.iterator();
					while(iter.hasNext()) {
						SystemPrivilegeInfo info = iter.next();
						if (info.getId().equals(this.getId())){
							iter.remove();
							break;
						}
					}
				}
			}
		}
	}
}