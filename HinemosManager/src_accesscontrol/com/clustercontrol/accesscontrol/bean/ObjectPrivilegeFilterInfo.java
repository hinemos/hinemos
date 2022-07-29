/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * Hinemosのオブジェクト権限検索条件を格納するクラス。<BR>
 * DTOクラスとしてマネージャ、クライアント間の通信で利用する。
 *
 */
@XmlType(namespace = "http://access.ws.clustercontrol.com")
public class ObjectPrivilegeFilterInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String objectType;
	private String objectId;
	private String roleId;
	private String objectPrivilege;

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public String getRoleId() {
		return roleId;
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public String getObjectPrivilege() {
		return objectPrivilege;
	}
	public void setObjectPrivilege(String objectPrivilege) {
		this.objectPrivilege = objectPrivilege;
	}
}