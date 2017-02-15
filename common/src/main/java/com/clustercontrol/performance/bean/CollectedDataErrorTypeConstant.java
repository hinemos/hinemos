/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

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
}
