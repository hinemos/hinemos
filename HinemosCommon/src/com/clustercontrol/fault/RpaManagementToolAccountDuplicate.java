/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPA管理ツールアカウントが重複している場合に利用するException
 */
public class RpaManagementToolAccountDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = 1L;
	private String m_rpaScopeId = null;

	/**
	 * コンストラクタ
	 */
	public RpaManagementToolAccountDuplicate() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public RpaManagementToolAccountDuplicate(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public RpaManagementToolAccountDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public RpaManagementToolAccountDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getscenarioId() {
		return m_rpaScopeId;
	}

	public void setScenarioId(String scenarioId) {
		m_rpaScopeId = scenarioId;
	}
}
