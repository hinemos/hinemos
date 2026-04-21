/*
 * Copyright (c) 2025 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.util;

/**
 * この関数型インターフェースは、再試行処理(RetryStrategy)と共に使用され、 成功するまで、または再試行の制限に達するまで
 * 操作を複数回実行する必要がある場合に利用されます。
 *
 * @param <T> 関数が返す結果の型
 */
public interface RetryFunction<T> {
	/**
	 * 再実行する処理を定義します。
	 *
	 * このメソッドは再実行が必要な操作をカプセル化します。 
	 *
	 * @return 実行結果
	 */
	T execute();
}
