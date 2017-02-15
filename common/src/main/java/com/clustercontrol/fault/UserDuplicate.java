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
 * userIDが重複している場合に利用するException
 * @version 3.2.0
 */
public class UserDuplicate extends HinemosException {

	private static final long serialVersionUID = -5612525221540116629L;
	private String m_userId = null;

	/**
	 * UserDuplicateExceptionコンストラクタ
	 */
	public UserDuplicate() {
		super();
	}

	/**
	 * UserDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public UserDuplicate(String messages) {
		super(messages);
	}

	/**
	 * UserDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public UserDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * UserDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public UserDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getUserId() {
		return m_userId;
	}

	public void setUserId(String userId) {
		m_userId = userId;
	}
}
