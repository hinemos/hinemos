/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
