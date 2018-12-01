/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

/**
 * 監視結果の取得値の計算方法に関する定数クラス<BR>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class ConvertValueConstant {

	/** 加工しない */
	public static final int TYPE_NO = 0;
	
	/** 差をとる */
	public static final int TYPE_DELTA = 1;

	private ConvertValueConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
