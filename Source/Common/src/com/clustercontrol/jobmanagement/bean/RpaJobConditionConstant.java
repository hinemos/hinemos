/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * RPAシナリオジョブ（間接実行）の実行状態を定義するクラス
 */

public class RpaJobConditionConstant {

	/** RPAシナリオジョブを開始した状態 */
	public static final int START_JOB = 0;

	/** RPA管理ツールの処理実行APIを呼び出した状態 */
	public static final int EXEC_RUN = 1;

}
