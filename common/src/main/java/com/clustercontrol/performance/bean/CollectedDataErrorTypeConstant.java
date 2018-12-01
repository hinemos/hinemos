/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.bean;

/**
 * 計算済み性能値の値が不正（NaN）の場合のエラーパターンを定義
 *
 * @version 3.2.0
 * @since 3.2.0
 */
public class CollectedDataErrorTypeConstant {

	// 正常
	public static final int NOT_ERROR = 0;

	// ポーリングの実行回数が不足している
	public static final int NOT_ENOUGH_COLLECT_COUNT = 1;

	// ポーリングがタイムアウトして値が取得できなかった
	public static final int POLLING_TIMEOUT = 2;

	// 対象のファシリティIDが存在しない
	public static final int FACILITY_NOT_FOUND = 3;

	// その他不明
	public static final int UNKNOWN = -1;

	private CollectedDataErrorTypeConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
