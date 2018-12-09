/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 変化点監視の計算ユーティリティクラス<br/>
 * 
 * @since 6.0.1
 */
public class OperatorChangeUtil {

	private static Log m_log = LogFactory.getLog(OperatorChangeUtil.class);

	/**
	 * 収集データより標準偏差＋平均を取得する<br/>
	 * 
	 * @param average 平均
	 * @param standardDeviation 標準偏差
	 * @param sigma 標準偏差（σ）の倍率
	 * @return 標準偏差＋平均
	 */
	public static Double getStandardDeviation(Double average, Double standardDeviation, Double sigma)  {
		Double rtn = null;
		if (average == null
				|| standardDeviation == null
				|| sigma == null) {
			return rtn;
		}
		// 標準偏差を計算する
		rtn = standardDeviation * sigma + average;
		return rtn;
	}

	/**
	 * 収集値、標準偏差、平均より変化量を取得する<br/>
	 * 
	 * @param value 収集値
	 * @param average 平均
	 * @param standardDeviation 標準偏差
	 * @return 標準偏差＋平均
	 */
	public static Double getChangeMount(Double value, Double average, Double standardDeviation)  {
		Double rtn = null;
		if (value == null
				|| value.isNaN()
				|| average == null
				|| average.isNaN()
				|| standardDeviation == null
				|| standardDeviation.isNaN()) {
			return rtn;
		}
		try {
			if (standardDeviation.doubleValue() == 0D) {
				if (value.doubleValue() == average.doubleValue()) {
					rtn = 0D;
				}
			} else {
				rtn = (value.doubleValue() - average.doubleValue()) / standardDeviation.doubleValue();
			}
		} catch (Exception e) {
			m_log.warn("getChangeMount(): changeMount could not be calculated.", e);
		}
		return rtn;
	}
}
