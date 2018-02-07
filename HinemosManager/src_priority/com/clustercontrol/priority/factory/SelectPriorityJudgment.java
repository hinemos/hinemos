/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.priority.factory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.priority.model.PriorityJudgmentInfo;
import com.clustercontrol.priority.util.QueryUtil;

/**
 * 重要度判定を検索するクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class SelectPriorityJudgment {

	/**
	 * 重要度判定情報を取得します。<BR>
	 * 
	 * @return
	 * @throws MonitorNotFound
	 */
	public PriorityJudgmentInfo getPriorityJudgment(String judgmentId) throws MonitorNotFound {
		//重要度判定情報を検索し取得
		return QueryUtil.getPriorityInfoPK(judgmentId);
	}
}
