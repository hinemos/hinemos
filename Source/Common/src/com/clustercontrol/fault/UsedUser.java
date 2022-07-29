/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ユーザが使用されている場合に利用するException
 * @version 4.1.0
 */
public class UsedUser extends HinemosUsed {

	private static final long serialVersionUID = 1L;

	private String m_userId = null;

	/**
	 * UsedUserコンストラクタ
	 */
	public UsedUser() {
		super();
	}

	/**
	 * UsedUserコンストラクタ
	 * @param messages
	 * @param e
	 */
	public UsedUser(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * UsedUserコンストラクタ
	 * @param messages
	 */
	public UsedUser(String messages) {
		super(messages);
	}

	/**
	 * UsedUserコンストラクタ
	 * @param e
	 */
	public UsedUser(Throwable e) {
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
