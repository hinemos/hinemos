/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck;

/**
 * セルフチェック機能における非同期処理の監視定義クラス
 */
public class AsyncTaskQueueConfig {

	// 非同期処理のworker名
	public final String worker;

	// 待ち処理数の通知閾値
	public final int queueThreshold;

	public AsyncTaskQueueConfig(String worker, int queueThresdhold) {
		this.worker = worker;
		this.queueThreshold = queueThresdhold;
	}

}
