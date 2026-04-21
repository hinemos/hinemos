/*
 * Copyright (c) 2025 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.util;

/**
 * 指定されたRetryFunctionをどのように再試行するかを実装します。
 *
 * @param <T> 再試行する操作が返す結果の型
 */
public interface RetryStrategy {
	/**
	 * 指定されたアクションを再試行
	 *
	 * @param action 再試行対象
	 * @param <T> actionが返す結果の型
	 * @return actionが成功時に返す結果
	 */
	<T> T executeWithRetry(RetryFunction<T> action);
}
