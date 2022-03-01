/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブの繰り返し待ち状態を定義する定数クラス
 */
public class RetryWaitStatusConstant {

	/** リトライ待ちでない */
	public static final int NONE = 0;

	/** リトライ待ち中 */
	public static final int WAIT = 1;

	/** 親ジョブセッションがリトライ待ち中 */
	public static final int PARENT_WAIT = 2;
}
