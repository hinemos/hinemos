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
 * ユーザが存在しない場合に利用するException
 * @version 3.2.0
 */
public class UserNotFound extends HinemosException {

	private static final long serialVersionUID = -2084566019791245622L;

	private String m_userId = null;

	/**
	 * UserNotFoundExceptionコンストラクタ
	 */
	public UserNotFound() {
		super();
	}

	/**
	 * UserNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public UserNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * UserNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public UserNotFound(String messages) {
		super(messages);
	}

	/**
	 * UserNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public UserNotFound(Throwable e) {
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
