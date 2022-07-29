/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa.scenariojob;

import org.openapitools.client.model.AgtRpaJobEndValueConditionInfoResponse;

/**
 * 終了値判定条件確認方法のインターフェース
 */
public interface EndValueCheckStrategy {

	/**
	 * 終了値判定条件を満たしているかどうかを返します。
	 * 
	 * @param endValueCondition
	 * @return true: 条件を満たしている / false: 条件を満たしていない
	 */
	boolean isSatisfied(AgtRpaJobEndValueConditionInfoResponse endValueCondition);

	/**
	 * 対象となる終了値判定条件タイプを返します。
	 * 
	 * @return 終了値判定条件タイプ
	 */
	int getConditionType();
}
