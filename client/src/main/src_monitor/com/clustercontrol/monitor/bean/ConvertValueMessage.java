/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
