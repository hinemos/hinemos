/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * リソース制御ジョブの実行状態を定義するクラス
 */
public class ResourceJobConditionConstant {

	/** リソース制御ジョブを開始した状態 */
	public static final int START_JOB = 0;

	/** リソース制御APIを実行した状態 */
	public static final int EXEC_API = 1;
}
