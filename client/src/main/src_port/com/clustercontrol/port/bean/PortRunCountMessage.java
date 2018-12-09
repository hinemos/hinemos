/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.port.bean;

import com.clustercontrol.util.Messages;

/**
 * port実行回数の定義を定数として格納するクラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class PortRunCountMessage {

	/** 1回（種別）。 */
	public static final int TYPE_COUNT_01 = 1;

	/** 2回（種別）。 */
	public static final int TYPE_COUNT_02 = 2;

	/** 3回（種別）。 */
	public static final int TYPE_COUNT_03 = 3;


	/** 1回（文字列）。 */
	public static final String STRING_COUNT_01 = TYPE_COUNT_01 + Messages.getString("count");

	/** 2回（文字列）。 */
	public static final String STRING_COUNT_02 = TYPE_COUNT_02 + Messages.getString("count");

	/** 3回（文字列）。 */
	public static final String STRING_COUNT_03 = TYPE_COUNT_03 + Messages.getString("count");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == TYPE_COUNT_01) {
			return STRING_COUNT_01;
		} else if (type == TYPE_COUNT_02) {
			return STRING_COUNT_02;
		} else if (type == TYPE_COUNT_03) {
			return STRING_COUNT_03;
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
			return TYPE_COUNT_01;
		} else if (string.equals(STRING_COUNT_02)) {
			return TYPE_COUNT_02;
		} else if (string.equals(STRING_COUNT_03)) {
			return TYPE_COUNT_03;
		}
		return -1;
	}
}