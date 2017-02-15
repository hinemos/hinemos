/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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