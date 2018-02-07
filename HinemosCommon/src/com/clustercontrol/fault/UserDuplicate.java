/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
