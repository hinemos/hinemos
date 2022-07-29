/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * Rpaシナリオ作成設定が重複している場合に利用するException
 */
public class RpaScenarioOperationResultCreateSettingDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = 2427308671054127843L;

	/**
	 * RpaScenarioOperationResultCreateSettingDuplicateコンストラクタ
	 */
	public RpaScenarioOperationResultCreateSettingDuplicate() {
		super();
	}

	/**
	 * RpaScenarioOperationResultCreateSettingDuplicateコンストラクタ
	 * @param createSettingId
	 */
	public RpaScenarioOperationResultCreateSettingDuplicate(String createSettingId) {
		super(createSettingId);
	}

	/**
	 * RpaScenarioOperationResultCreateSettingDuplicateコンストラクタ
	 * @param e
	 */
	public RpaScenarioOperationResultCreateSettingDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * RpaScenarioOperationResultCreateSettingDuplicateコンストラクタ
	 * @param messages
	 * @param e
	 */
	public RpaScenarioOperationResultCreateSettingDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

}
