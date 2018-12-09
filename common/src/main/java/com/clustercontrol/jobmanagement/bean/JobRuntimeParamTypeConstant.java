/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブ[実行契機]の変数種別定数クラス<BR>
 * 
 * @version 5.1.0
 */
public class JobRuntimeParamTypeConstant {
	/** 入力の場合 */
	public static final int TYPE_INPUT= 0;

	/** 選択（ラジオボタン）の場合 */
	public static final int TYPE_RADIO = 1;

	/** 選択（コンボボックス）の場合 */
	public static final int TYPE_COMBO = 2;

	/** 固定の場合 */
	public static final int TYPE_FIXED = 3;

	private JobRuntimeParamTypeConstant() {
		throw new IllegalStateException("ConstClass");
	}
}