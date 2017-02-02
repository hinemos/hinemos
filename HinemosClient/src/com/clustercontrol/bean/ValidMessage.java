/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.bean;

import com.clustercontrol.util.Messages;

/**
 * 有効／無効の定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ValidMessage {
	/** 有効（文字列）。 */
	public static final String STRING_VALID = Messages.getString("valid");

	/** 無効（文字列）。 */
	public static final String STRING_INVALID = Messages.getString("invalid");

	/**
	 * 種別から文字列に変換します。
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(boolean type) {
		if (type) {
			return STRING_VALID;
		} else {
			return STRING_INVALID;
		}
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static boolean stringToType(String string) {
		if (string.equals(STRING_VALID)) {
			return true;
		} else {
			return false;
		}
	}
}