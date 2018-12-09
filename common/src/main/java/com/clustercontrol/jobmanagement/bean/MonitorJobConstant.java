/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;


/**
 * 監視ジョブの初期値を定義するクラス<BR>
 *
 * @version 6.0.0
 */
public class MonitorJobConstant {
	/** 戻り値（情報） */
	public static final int INITIAL_END_VALUE_INFO = 0;
	/** 戻り値（警告） */
	public static final int INITIAL_END_VALUE_WARN = 1;
	/** 戻り値（危険） */
	public static final int INITIAL_END_VALUE_CRITICAL = 9;
	/** 戻り値（不明） */
	public static final int INITIAL_END_VALUE_UNKNOWN = -1;
	/** 待ち間隔 */
	public static final int INITIAL_WAIT_INTERVAL_MINUTE = 1;

	private MonitorJobConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
