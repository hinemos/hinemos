/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.selfcheck;

import com.clustercontrol.selfcheck.monitor.TableSizeMonitor.ThresholdType;

/**
 * セルフチェック機能におけるテーブルサイズの監視定義クラス
 */
public class TableSizeConfig {

	// 監視対象となるテーブル名
	public final String tableName;

	// 監視対象とする値の種別（行数 or 物理ファイルサイズ)
	public final ThresholdType thresdholdType;

	// 通知閾値
	public final long threshold;

	public TableSizeConfig(String tableName, ThresholdType thresholdType, long threshold) {
		this.tableName = tableName;
		this.thresdholdType = thresholdType;
		this.threshold = threshold;
	}

}
