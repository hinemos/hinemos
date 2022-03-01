/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.bean;

/**
 * RPAシナリオジョブの終了値判定条件の定数クラス
 */
public class RpaJobEndValueConditionTypeConstant {
	/** ログから判定 */
	public static final int LOG = 0;
	/** リターンコードから判定 */
	public static final int RETURN_CODE = 1;
}
