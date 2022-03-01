/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RpaScenarioが重複している場合に利用するException
 */
public class RpaScenarioDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = 2427308671054127843L;
	private String m_scenarioId = null;

	/**
	 * RpaScenarioDuplicateコンストラクタ
	 */
	public RpaScenarioDuplicate() {
		super();
	}

	/**
	 * RpaScenarioDuplicateコンストラクタ
	 * @param messages
	 */
	public RpaScenarioDuplicate(String messages) {
		super(messages);
	}

	/**
	 * RpaScenarioDuplicateコンストラクタ
	 * @param e
	 */
	public RpaScenarioDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * RpaScenarioDuplicateコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RpaScenarioDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getscenarioId() {
		return m_scenarioId;
	}

	public void setScenarioId(String scenarioId) {
		m_scenarioId = scenarioId;
	}
}
