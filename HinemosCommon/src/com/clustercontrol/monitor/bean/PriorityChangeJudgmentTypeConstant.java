/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.monitor.bean;

public class PriorityChangeJudgmentTypeConstant {

	/** 重要度変化しない */
	public static final int TYPE_NOT_PRIORITY_CHANGE = 0;

	/** 監視詳細をまたいて重要度変化する */
	public static final int TYPE_ACROSS_MONITOR_DETAIL_ID = 1;

}
