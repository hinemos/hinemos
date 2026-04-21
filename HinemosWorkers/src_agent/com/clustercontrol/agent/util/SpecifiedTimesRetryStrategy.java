/*
 * Copyright (c) 2025 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 指定された回数まで操作を再試行し、null以外の結果を得られた場合は 結果を返します。
 */
public class SpecifiedTimesRetryStrategy implements RetryStrategy {
	private static Log log = LogFactory.getLog(SpecifiedTimesRetryStrategy.class);
	private int maxRetry = 0;

	/**
	 *
	 * @param maxRetry
	 *            最大再試行回数
	 */
	public SpecifiedTimesRetryStrategy(int maxRetry) {
		this.maxRetry = maxRetry;
	}

	/**
	 * 指定されたactionを再実行します。 再試行回数がmaxRetryに達するか、null以外の結果を得られるまで処理を続けます。
	 *
	 * @param action 再試行対象
	 * @param <T> actionが返す結果の型
	 * @return actionが成功し取得された結果
	 *            再試行限界を超えた場合に nullを返します。
	 */
	@Override
	public <T> T executeWithRetry(RetryFunction<T> action) {
		T result = null;
		for (int i = 0; i < maxRetry; i++) {
			try {
				result = action.execute();
				if (result != null) {
					break;
				}
			} catch (Exception e) {
				log.warn("executeWithRetry(): Failed to get. reason=" + e.getClass().getSimpleName() + e.getMessage());
			}
			log.debug("executeWithRetry(): Failed to get, so we will retry. count=" + i);
		}

		return result;
	}
}
