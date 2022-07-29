/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ping.bean;

import com.clustercontrol.util.Messages;

/**
 * ping実行回数の定義を定数として格納するクラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class PingRunCountMessage {
	/** 1回（文字列）。 */
	public static final String STRING_COUNT_01 = PingRunCountConstant.TYPE_COUNT_01 + Messages.getString("count");

	/** 2回（文字列）。 */
	public static final String STRING_COUNT_02 = PingRunCountConstant.TYPE_COUNT_02 + Messages.getString("count");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == PingRunCountConstant.TYPE_COUNT_01) {
			return STRING_COUNT_01;
		} else if (type == PingRunCountConstant.TYPE_COUNT_02) {
			return STRING_COUNT_02;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_COUNT_01)) {
			return PingRunCountConstant.TYPE_COUNT_01;
		} else if (string.equals(STRING_COUNT_02)) {
			return PingRunCountConstant.TYPE_COUNT_02;
		}
		return -1;
	}
}