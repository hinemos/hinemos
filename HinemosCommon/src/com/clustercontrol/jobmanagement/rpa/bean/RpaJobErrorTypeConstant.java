/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.rpa.bean;

/**
 * RPAシナリオジョブの異常の種類を表す定数クラス
 */
public class RpaJobErrorTypeConstant {
	/** ログインされていない（RPAツールエグゼキューターが起動していない ） */
	public static final int NOT_LOGIN = 0;
	/** RPAツールが既に起動している */
	public static final int ALREADY_RUNNING = 1;
	/** RPAツールが異常終了した */
	public static final int ABNORMAL_EXIT = 2;
	/** それ以外 */
	public static final int OTHER = 3;
}
