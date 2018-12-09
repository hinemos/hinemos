/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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