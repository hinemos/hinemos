/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.util;

import com.clustercontrol.bean.EndStatusConstant;

public class JobCommonUtil {

	/**
	 * 終了値から終了状態を判定し返します。
	 * 
	 * @param sessionJob
	 * @param endValue
	 * @return 終了状態
	 */
	public static Integer checkEndStatus(int endValue, int normalFrom, int normalTo, int warnFrom, int warnTo) {
		if (endValue >= normalFrom && endValue <= normalTo) {
			// 終了状態（正常）の範囲内ならば、正常とする
			return EndStatusConstant.TYPE_NORMAL;
		} else if (endValue >= warnFrom && endValue <= warnTo) {
			// 終了状態（警告）の範囲内ならば、警告とする
			return EndStatusConstant.TYPE_WARNING;
		} else {
			// 終了状態（異常）の範囲内ならば、異常とする
			return EndStatusConstant.TYPE_ABNORMAL;
		}
	}

}
