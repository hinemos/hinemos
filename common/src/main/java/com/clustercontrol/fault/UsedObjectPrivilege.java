/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.fault;

/**
 * オブジェクト権限が監視等で利用されている場合に利用するException
 */
public class UsedObjectPrivilege extends HinemosException {

	private static final long serialVersionUID = 1L;

	/**
	 * オブジェクト種別
	 */
	private String objectType = "";

	/**
	 * オブジェクトID
	 */
	private String objectId = "";

	/**
	 * UsedObjectPrivilegeコンストラクタ
	 */
	public UsedObjectPrivilege() {
		super();
	}

	/**
	 * UsedObjectPrivilegeコンストラクタ
	 * @param messages
	 */
	public UsedObjectPrivilege(String messages) {
		super(messages);
	}

	/**
	 * UsedObjectPrivilegeコンストラクタ
	 * @param objectType
	 * @param objectId
	 */
	public UsedObjectPrivilege(String objectType, String objectId) {
		super();
		this.objectType = objectType;
		this.objectId = objectId;
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

}
