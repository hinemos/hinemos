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

package com.clustercontrol.monitor.bean;

/**
 * イベント情報の性能グラフ用フラグの定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class CollectGraphFlgMessage {
	/** ON */
	public static final String STRING_FLG_ON = "ON";

	/** OFF */
	public static final String STRING_FLG_OFF = "OFF";


	/** ON */
	public static final boolean BOOLEAN_FLG_ON = true;

	/** OFF */
	public static final boolean BOOLEAN_FLG_OFF = false;

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(Boolean type) {
		if (CollectGraphFlgConstant.TYPE_ON.equals(type)) {
			return STRING_FLG_ON;
		} else if (CollectGraphFlgConstant.TYPE_OFF.equals(type)) {
			return STRING_FLG_OFF;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static Boolean stringToType(String string) {
		if (STRING_FLG_ON.equals(string)) {
			return CollectGraphFlgConstant.TYPE_ON;
		} else if (STRING_FLG_OFF.equals(string)) {
			return CollectGraphFlgConstant.TYPE_OFF;
		}
		return CollectGraphFlgConstant.TYPE_ALL;
	}
}