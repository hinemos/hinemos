/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosArithmeticException;
import com.clustercontrol.fault.HinemosIllegalArgumentException;

/**
 * サイレント障害監視関連の計算ユーティリティクラス<br/>
 * 
 * @since 6.0.1
 */
public class OperatorAnalyticsUtil {

	private static Log m_log = LogFactory.getLog(OperatorAnalyticsUtil.class);

	/**
	 * 相関係数を算出する<br/>
	 * 
	 * @param map 演算対象
	 * @param dataCount 最小データ数
	 * @return 共分散
	 */
	public static Double getCorrelationCoefficient(Map<Long, Double[]> map, int dataCount) 
			throws HinemosIllegalArgumentException, HinemosArithmeticException {
		Double rtn = 0D;
		if (map == null || map.size() <= 0) {
			throw new HinemosIllegalArgumentException("getCorrelationCoefficient() : There is no effective data.");
		}

		try {
			// 必要な情報を取り出す
			List<Double> tmpList1 = new ArrayList<>();
			List<Double> tmpList2 = new ArrayList<>();
			for (Map.Entry<Long, Double[]> entry : map.entrySet()) {
				if (entry.getValue()[0] != null && entry.getValue()[1] != null
						&& !entry.getValue()[0].isNaN() && !entry.getValue()[1].isNaN() ) {
					tmpList1.add(entry.getValue()[0]);
					tmpList2.add(entry.getValue()[1]);
				}
			}

			// データ数が少ない場合はエラー
			if (tmpList1.size() < dataCount) {
				throw new HinemosIllegalArgumentException("getCorrelationCoefficient() : "
						+ "The number of data is insufficient."
						+ " required count=" + dataCount
						+ ", data count=" + tmpList1.size());
			}

			// 共分散
			Double covariance = getCovariance(tmpList1, tmpList2);
	
			// 標準偏差の合計
			Double standardDeviations = OperatorCommonUtil.getStandardDeviation(tmpList1) 
					* OperatorCommonUtil.getStandardDeviation(tmpList2);
	
			rtn = covariance/standardDeviations;
		} catch (HinemosIllegalArgumentException | HinemosArithmeticException e) {
			throw e;
		} catch (Exception e) {
			throw new HinemosArithmeticException("getCorrelationCoefficient() : Failed in the calculation process."
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage());
		}	
		m_log.debug("getCorrelationCoefficient correlationCoefficient=" + rtn);
		return rtn;
	}

	/**
	 * 共分散を算出する<br/>
	 * 
	 * @param list1 算出対象１
	 * @param list2 算出対象２
	 * @return 共分散
	 */
	private static Double getCovariance(List<Double> list1, List<Double> list2) 
			throws HinemosIllegalArgumentException, HinemosArithmeticException {
		// 共分散
		Double rtn = 0D;

		if (list1 == null || list1.size() <= 0
				|| list2 == null || list2.size() <= 0
				|| list1.size() != list2.size()) {
			throw new HinemosIllegalArgumentException("getCovariance() : There is no effective data.");
		}
		try {
			// 平均
			Double average1 = OperatorCommonUtil.getAverage(list1);
			Double average2 = OperatorCommonUtil.getAverage(list2);
	
			// 共分散
			for (int i = 0; i < list1.size(); i++) {
				m_log.debug("getCovariance i=" + i 
						+ ", average1=" + average1 
						+ ", list1=" + list1.get(i) 
						+ ", average2=" + average2 
						+ ", list2=" + list2.get(i));
				rtn += (list1.get(i).doubleValue() - average1)*(list2.get(i).doubleValue() - average2);
			}
			rtn /= list1.size();
			m_log.debug("getCovariance covariance=" + rtn);
		} catch (HinemosIllegalArgumentException | HinemosArithmeticException e) {
			throw e;
		} catch (Exception e) {
			throw new HinemosArithmeticException("getCovariance() : Failed in the calculation process." 
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage());
		}	
		return rtn;
	}
}
