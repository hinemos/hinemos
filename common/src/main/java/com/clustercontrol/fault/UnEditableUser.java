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
 * 変更・削除してはいけないユーザの場合に利用するException
 * @version 4.1.0
 */
public class UnEditableUser extends HinemosException {

	private static final long serialVersionUID = 1L;

	private String m_userId = null;

	/**
	 * UnEditableUserコンストラクタ
	 */
	public UnEditableUser() {
		super();
	}

	/**
	 * UnEditableUserコンストラクタ
	 * @param messages
	 * @param e
	 */
	public UnEditableUser(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * UnEditableUserコンストラクタ
	 * @param messages
	 */
	public UnEditableUser(String messages) {
		super(messages);
	}

	/**
	 * UnEditableUserコンストラクタ
	 * @param e
	 */
	public UnEditableUser(Throwable e) {
		super(e);
	}

	/**
	 * ユーザIDを返します。
	 * @return ユーザID
	 */
	public String getUserId() {
		return m_userId;
	}

	/**
	 * ユーザIDを設定します。
	 * @param userId ユーザID
	 */
	public void setUserId(String userId) {
		m_userId = userId;
	}

}
