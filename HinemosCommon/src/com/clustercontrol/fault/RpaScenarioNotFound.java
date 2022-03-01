/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPAシナリオが存在しない場合に利用するException
 */
public class RpaScenarioNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 2387815483607910087L;
	private String m_scenarioId = null;

	/**
	 * RpaScenarioNotFoundExceptionコンストラクタ
	 */
	public RpaScenarioNotFound() {
		super();
	}

	/**
	 * RpaScenarioNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RpaScenarioNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * RpaScenarioNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public RpaScenarioNotFound(String messages) {
		super(messages);
	}

	/**
	 * RpaScenarioNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public RpaScenarioNotFound(Throwable e) {
		super(e);
	}

	/**
	 * RPAシナリオIDを返します。
	 * @return RPAシナリオID
	 */
	public String getScenarioId() {
		return m_scenarioId;
	}

	/**
	 * RPAシナリオIDを設定します。
	 * @param scenarioId メンテナンスID
	 */
	public void setScenarioId(String scenarioId) {
		m_scenarioId = scenarioId;
	}




}
