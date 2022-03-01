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
import com.clustercontrol.fault.RpaScenarioNotFound;
import com.clustercontrol.rpa.model.RpaScenarioCoefficientPattern;
import com.clustercontrol.rpa.util.QueryUtil;

/**
 * 自動化効果計算マスタ情報を検索するクラス
 *
 */
public class SelectRpaScenarioCoefficientPattern {

	/**
	 * 自動化効果計算マスタ情報を返します。
	 */
	public RpaScenarioCoefficientPattern getRpaScenarioCoefficientPattern(String rpaToolEnvId, int orderNo) throws RpaScenarioNotFound, InvalidRole {
		RpaScenarioCoefficientPattern entity = null;
		entity = QueryUtil.getRpaScenarioCoefficientPattern(rpaToolEnvId, orderNo);
		
		return entity;
	}

	/**
	 * 自動化効果計算マスタ情報一覧を返します。
	 */
	public List<RpaScenarioCoefficientPattern> getRpaScenarioTagList() {
		List<RpaScenarioCoefficientPattern> entityList = 
				QueryUtil.getRpaScenarioCoefficientPatternList();
		return entityList;
	}
}
