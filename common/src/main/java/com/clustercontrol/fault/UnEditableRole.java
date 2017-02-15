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
 * 変更・削除してはいけないロールの場合に利用するException
 * @version 4.1.0
 */
public class UnEditableRole extends HinemosException {

	private static final long serialVersionUID = 1L;

	private String m_roleId = null;

	/**
	 * UnEditableRoleコンストラクタ
	 */
	public UnEditableRole() {
		super();
	}

	/**
	 * UnEditableRoleコンストラクタ
	 * @param messages
	 * @param e
	 */
	public UnEditableRole(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * UnEditableRoleコンストラクタ
	 * @param messages
	 */
	public UnEditableRole(String messages) {
		super(messages);
	}

	/**
	 * UnEditableRoleコンストラクタ
	 * @param e
	 */
	public UnEditableRole(Throwable e) {
		super(e);
	}

	/**
	 * ロールIDを返します。
	 * @return ロールID
	 */
	public String getRoleId() {
		return m_roleId;
	}

	/**
	 * ロールIDを設定します。
	 * @param roleId ロールID
	 */
	public void setRoleId(String roleId) {
		m_roleId = roleId;
	}

}
