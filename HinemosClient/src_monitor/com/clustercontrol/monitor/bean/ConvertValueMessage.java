/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import com.clustercontrol.util.Messages;

/**
 * 監視結果の取得値の計算方法に関する定数クラス<BR>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class ConvertValueMessage {
	/** 加工しない */
	public static final String STRING_NO = Messages.getString("convert.no");

	/** 差をとる */
	public static final String STRING_DELTA = Messages.getString("delta");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == ConvertValueConstant.TYPE_NO) {
			return STRING_NO;
		} else if (type == ConvertValueConstant.TYPE_DELTA) {
			return STRING_DELTA;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_NO)) {
			return ConvertValueConstant.TYPE_NO;
		} else if (string.equals(STRING_DELTA)) {
			return ConvertValueConstant.TYPE_DELTA;
		}
		return -1;
	}
}
