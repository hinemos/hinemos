/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブキュー(同時実行制御キュー)に関連するコンポーネント共通定数です。
 *
 * @since 6.2.0
 */
public class JobQueueConstant {

	/** キューIDの最大長 */
	public static final int ID_MAXLEN = 64;

	/** キュー名の最大長 */
	public static final int NAME_MAXLEN = 64;

	/** 同時実行可能数の最小値 */
	public static final int CONCURRENCY_MIN = 0;
	
	/** 同時実行可能数の最大値 */
	public static final int CONCURRENCY_MAX = 9999;
}
