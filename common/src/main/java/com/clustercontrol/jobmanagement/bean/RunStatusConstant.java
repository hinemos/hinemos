/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブエージェントのコマンド実行状態の定数を定義するクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class RunStatusConstant {
	/** 開始 */
	public static final int START = 0;
	/** 終了 */
	public static final int END = 1;
	/** 失敗 */
	public static final int ERROR = 2;

	private RunStatusConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
