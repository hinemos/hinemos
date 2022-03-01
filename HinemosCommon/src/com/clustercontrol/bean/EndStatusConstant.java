/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

/**
 * ジョブ終了状態の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class EndStatusConstant {
	/** 正常 */
	public static final int TYPE_NORMAL = 0;

	/** 警告 */
	public static final int TYPE_WARNING = 1;

	/** 異常 */
	public static final int TYPE_ABNORMAL = 2;

	/** 開始 */
	public static final int TYPE_BEGINNING = 3;

	/** すべての終了状態 */
	public static final int TYPE_ANY = 4;

	/** なし */
	public static final int TYPE_NONE = -1;

	/** 正常 */
	public static final int INITIAL_VALUE_NORMAL = 0;

	/** 警告 */
	public static final int INITIAL_VALUE_WARNING = 1;

	/** 異常 */
	public static final int INITIAL_VALUE_ABNORMAL = -1;
}