/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * リソース制御ジョブの初期値を定義するクラス
 */
public class ResourceJobConstant {

	/** 状態確認期間（秒） */
	public static final int STATUS_CONFIRM_TIME = 300;

	/** 状態確認間隔（秒） */
	public static final int STATUS_CONFIRM_INTERVAL = 5;

	/** 終了値（成功） */
	public static final int SUCCESS_VALUE = 0;

	/** 終了値（失敗） */
	public static final int FAILURE_VALUE = -1;
}
