/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * オブジェクト権限チェックを行うクラスのMappedSuperClass
 * 
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
@MappedSuperclass
@EntityListeners(value={EntityListener.class})
public class ObjectPrivilegeTargetInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String objectId;
	private String ownerRoleId;
	private boolean uncheckFlg = false;

	public ObjectPrivilegeTargetInfo() {
	}
	
	@XmlTransient
	public String getObjectId() {
		return this.objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	@Column(name="owner_role_id")
	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public boolean tranGetUncheckFlg() {
		return uncheckFlg;
	}

	public void tranSetUncheckFlg(boolean uncheckFlg) {
		this.uncheckFlg = uncheckFlg;
	}
}