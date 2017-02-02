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
