/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ロールが使用されている場合に利用するException
 * @version 4.1.0
 */
public class UsedRole extends HinemosException {

	private static final long serialVersionUID = 1L;

	private String m_roleId = null;

	/**
	 * UsedRoleコンストラクタ
	 */
	public UsedRole() {
		super();
	}

	/**
	 * UsedRoleコンストラクタ
	 * @param messages
	 * @param e
	 */
	public UsedRole(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * UsedRoleコンストラクタ
	 * @param messages
	 */
	public UsedRole(String messages) {
		super(messages);
	}

	/**
	 * UsedRoleコンストラクタ
	 * @param e
	 */
	public UsedRole(Throwable e) {
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
