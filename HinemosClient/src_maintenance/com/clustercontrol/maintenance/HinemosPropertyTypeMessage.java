/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance;

import java.util.HashMap;

import com.clustercontrol.util.Messages;

/**
 * Hinemosプロパティ種別の定義を定数として格納するクラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class HinemosPropertyTypeMessage {
	/** 文字列（文字列）。 */
	public static final String STRING_STRING = Messages.getString("string");
	
	/** 数値（文字列）。 */
	public static final String STRING_NUMERIC = Messages.getString("numeric");

	/** 真偽値（文字列）。 */
	public static final String STRING_TRUTH = Messages.getString("truth");

	/**
	 * 種別から文字列に変換します。
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == HinemosPropertyTypeConstant.TYPE_TRUTH) {
			return STRING_TRUTH;
		} else if (type == HinemosPropertyTypeConstant.TYPE_NUMERIC) {
			return STRING_NUMERIC;
		} else if (type == HinemosPropertyTypeConstant.TYPE_STRING) {
			return STRING_STRING;
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
		if (string.equals(STRING_TRUTH)) {
			return HinemosPropertyTypeConstant.TYPE_TRUTH;
		} else if (string.equals(STRING_NUMERIC)) {
			return HinemosPropertyTypeConstant.TYPE_NUMERIC;
		} else if (string.equals(STRING_STRING)) {
			return HinemosPropertyTypeConstant.TYPE_STRING;
		}
		return -1;
	}
	
	/**
	 * 共通設定種別の一覧をMap形式で返します。
	 * @return 種別一覧
	 */
	public static HashMap<Integer, String> getList() {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		
		map.put(HinemosPropertyTypeConstant.TYPE_STRING, STRING_STRING);
		map.put(HinemosPropertyTypeConstant.TYPE_NUMERIC, STRING_NUMERIC);
		map.put(HinemosPropertyTypeConstant.TYPE_TRUTH, STRING_TRUTH);
		
		return map;
	}
}