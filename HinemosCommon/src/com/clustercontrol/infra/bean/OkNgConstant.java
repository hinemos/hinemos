/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.bean;

/**
 * OK/NG の定義を定数として格納するクラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class OkNgConstant {
	/** OK（種別）。 */
	public static final int TYPE_OK = 1;

	/** NG（種別）。 */
	public static final int TYPE_NG = 0;

	/** SKIP */
	public static final int TYPE_SKIP = 2;

	/** OK（文字列）。 */
	public static final String STRING_OK = "OK";

	/** NG（文字列）。 */
	public static final String STRING_NG = "NG";

	/** SKIP（文字列）。 */
	public static final String STRING_SKIP = "SKIP";

	/**
	 * 種別から文字列に変換します。
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == TYPE_OK) {
			return STRING_OK;
		} else if (type == TYPE_NG) {
			return STRING_NG;
		} else if (type == TYPE_SKIP) {
			return STRING_SKIP;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_OK)) {
			return TYPE_OK;
		} else if (string.equals(STRING_NG)) {
			return TYPE_NG;
		} else if (string.equals(STRING_SKIP)) {
			return TYPE_SKIP;
		}
		return -1;
	}
}