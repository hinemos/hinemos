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
 * ロールが存在しない場合に利用するException
 */
public class RoleNotFound extends HinemosException {

	private static final long serialVersionUID = 1L;

	private String m_roleId = null;

	/**
	 * RoleNotFoundExceptionコンストラクタ
	 */
	public RoleNotFound() {
		super();
	}

	/**
	 * RoleNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RoleNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * RoleNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public RoleNotFound(String messages) {
		super(messages);
	}

	/**
	 * RoleNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public RoleNotFound(Throwable e) {
		super(e);
	}

	/**
	 * ロールIDを返します。
	 * @return ユーザID
	 */
	public String getRoleId() {
		return m_roleId;
	}

	/**
	 * ロールIDを設定します。
	 * @param roleId ユーザID
	 */
	public void setRoleId(String roleId) {
		m_roleId = roleId;
	}




}
