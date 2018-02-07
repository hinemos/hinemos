/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosArithmeticException;
import com.clustercontrol.fault.HinemosIllegalArgumentException;

/**
 * 監視設定共通で使用する計算ユーティリティクラス<br/>
 * 
 * @since 6.0.1
 */
public class OperatorCommonUtil {

	private static Log m_log = LogFactory.getLog(OperatorCommonUtil.class);

	/**
	 * 標準偏差を算出する<br/>
	 * 
	 * @param array 算出対象
	 * @return 標準偏差
	 */
	public static Double getStandardDeviation(List<Double> list) 
			throws HinemosIllegalArgumentException, HinemosArithmeticException {
		Double rtn = null;
		if (list == null || list.size() <= 0) {
			throw new HinemosIllegalArgumentException("getStandardDeviation() : There is no effective data.");
		}
		
		try {
			// 平均
			Double average = getAverage(list);
	
			// 分散
			Double dispersion = 0D;
			for (int i = 0; i < list.size(); i++) {
				dispersion += Math.pow((list.get(i) - average), 2);
			}
			dispersion /= list.size();
			m_log.debug("getStandardDeviation array=" + list.toString()
			+ ", dispersion=" + dispersion);
	
			// 標準偏差
			rtn = Math.sqrt(dispersion);
			m_log.debug("getStandardDeviation array=" + list.toString()
			+ ", standardDeviation=" + rtn);

		} catch (HinemosIllegalArgumentException | HinemosArithmeticException e) {
			throw e;
		} catch (Exception e) {
			throw new HinemosArithmeticException("getStandardDeviation() : " 
					+ "Calculation processing failed., " + e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		
		return rtn;
	}

	/**
	 * 平均を算出する<br/>
	 * 
	 * @param array 算出対象
	 * @return 平均
	 */
	public static Double getAverage(List<Double> list) 
			throws HinemosIllegalArgumentException, HinemosArithmeticException {
		Double rtn = null;
		if (list == null) {
			throw new HinemosIllegalArgumentException("getAverage() : There is no effective data.");
		}
		try {
			double value = 0D;
			double count = 0D;
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) != null && !list.get(i).isNaN()) {
					count++;
					value += list.get(i).doubleValue(); 
				}
			}
			if (count == 0D) {
				throw new HinemosIllegalArgumentException("getAverage() : There is no effective data.");
			} else {
				rtn = value / count; 
			}
		} catch (HinemosIllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			throw new HinemosArithmeticException("getAverage() : " 
					+ "Calculation processing failed.," + e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		m_log.debug("getAverage array=" + list + ", average=" + rtn);
		return rtn;
	}
}
