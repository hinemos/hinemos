/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 変更・削除してはいけないロールの場合に利用するException
 * @version 4.1.0
 */
public class UnEditableRole extends HinemosException {

	private static final long serialVersionUID = 1L;

	private String m_roleId = null;

	/**
	 * UnEditableRoleコンストラクタ
	 */
	public UnEditableRole() {
		super();
	}

	/**
	 * UnEditableRoleコンストラクタ
	 * @param messages
	 * @param e
	 */
	public UnEditableRole(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * UnEditableRoleコンストラクタ
	 * @param messages
	 */
	public UnEditableRole(String messages) {
		super(messages);
	}

	/**
	 * UnEditableRoleコンストラクタ
	 * @param e
	 */
	public UnEditableRole(Throwable e) {
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
