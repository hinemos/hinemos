/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck;

/**
 * セルフチェック機能におけるファイルシステム使用率の監視定義クラス
 */
public class FileSystemUsageConfig {

	// 監視対象とするファイルシステムのマウントポイント
	public final String mountPoint;

	// ファイルシステム使用率の通知閾値
	public final int percentThreshold;

	public FileSystemUsageConfig(String mountPoint, int percentThreshold) {
		this.mountPoint = mountPoint;
		this.percentThreshold = percentThreshold;
	}

}
