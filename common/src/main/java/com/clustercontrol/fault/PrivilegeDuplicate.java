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
 * システム権限、オブジェクト権限が重複している場合に利用するException
 * @version 3.2.0
 */
public class PrivilegeDuplicate extends HinemosException {

	private static final long serialVersionUID = 1L;
	// オブジェクト権限のPK
	private String m_objectType = null;
	private String m_objectId = null;
	private String m_objectPrivilege = null;
	// システム権限のPK
	private String m_systemPrivilege = null;
	// ロールID
	private String m_roleId = null;

	/**
	 * PrivilegeDuplicateExceptionコンストラクタ
	 */
	public PrivilegeDuplicate() {
		super();
	}

	/**
	 * PrivilegeDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public PrivilegeDuplicate(String messages) {
		super(messages);
	}

	/**
	 * PrivilegeDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public PrivilegeDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * PrivilegeDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public PrivilegeDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getObjectType() {
		return m_objectType;
	}

	public void setObjectType(String objectType) {
		this.m_objectType = objectType;
	}

	public String getObjectId() {
		return m_objectId;
	}

	public void setObjectId(String objectId) {
		this.m_objectId = objectId;
	}

	public String getObjectPrivilege() {
		return m_objectPrivilege;
	}

	public void setObjectPrivilege(String objectPrivilege) {
		this.m_objectPrivilege = objectPrivilege;
	}

	public String getSystemPrivilege() {
		return m_systemPrivilege;
	}

	public void setSystemPrivilege(String systemPrivilege) {
		this.m_systemPrivilege = systemPrivilege;
	}

	public String getRoleId() {
		return m_roleId;
	}

	public void setRoleId(String roleId) {
		this.m_roleId = roleId;
	}

}
