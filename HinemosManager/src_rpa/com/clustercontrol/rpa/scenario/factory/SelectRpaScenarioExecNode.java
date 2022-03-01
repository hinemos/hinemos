/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.scenario.factory;

import java.util.List;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.rpa.scenario.model.RpaScenarioExecNode;
import com.clustercontrol.rpa.util.QueryUtil;

/**
 * RPAシナリオ情報を検索するクラス
 */
public class SelectRpaScenarioExecNode {

	/**
	 * 指定したシナリオ識別子およびシナリオ実績作成設定IDと一致する
	 * RPAシナリオ実行ノード情報を返します。
	 */
	public List<RpaScenarioExecNode> getRpaScenarioExecNode(String scenarioIdentifyString, String scenarioOperationResultCreateSettingId) throws InvalidRole {
		List<RpaScenarioExecNode> entities = null;
		entities = QueryUtil.getRpaScenarioExecNodeList(scenarioIdentifyString, scenarioOperationResultCreateSettingId);
		
		return entities;
	}
}
