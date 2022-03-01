/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ロールIDが重複している場合に利用するException
 * @version 3.2.0
 */
public class RoleDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = 1L;
	private String m_roleId = null;

	/**
	 * RoleDuplicateExceptionコンストラクタ
	 */
	public RoleDuplicate() {
		super();
	}

	/**
	 * RoleDuplicateExceptionコンストラクタ
	 * @param messages
	 */
	public RoleDuplicate(String messages) {
		super(messages);
	}

	/**
	 * RoleDuplicateExceptionコンストラクタ
	 * @param e
	 */
	public RoleDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * RoleDuplicateExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RoleDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getRoleId() {
		return m_roleId;
	}

	public void setRoleId(String roleId) {
		m_roleId = roleId;
	}
}
