/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
