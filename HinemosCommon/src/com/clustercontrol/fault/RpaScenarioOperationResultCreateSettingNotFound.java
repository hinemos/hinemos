/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * RPAシナリオ実績作成設定が存在しない場合に利用するException
 */
public class RpaScenarioOperationResultCreateSettingNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 2387815483607910087L;

	/**
	 * RpaScenarioOperationResultCreateSettingNotFoundコンストラクタ
	 */
	public RpaScenarioOperationResultCreateSettingNotFound() {
		super();
	}

	/**
	 * RpaScenarioOperationResultCreateSettingNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RpaScenarioOperationResultCreateSettingNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * RpaScenarioOperationResultCreateSettingNotFoundコンストラクタ
	 * @param createSettingId
	 */
	public RpaScenarioOperationResultCreateSettingNotFound(String createSettingId) {
		super(createSettingId);
	}

	/**
	 * RpaScenarioOperationResultCreateSettingNotFoundコンストラクタ
	 * @param e
	 */
	public RpaScenarioOperationResultCreateSettingNotFound(Throwable e) {
		super(e);
	}
}
