/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
