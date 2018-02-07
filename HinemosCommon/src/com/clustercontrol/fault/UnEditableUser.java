/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
