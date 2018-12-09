/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
