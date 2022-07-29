/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance;

import java.util.LinkedHashMap;

import org.openapitools.client.model.HinemosPropertyResponse.TypeEnum;

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
	public static String typeToString(TypeEnum type) {
		if (TypeEnum.BOOLEAN.equals(type)) {
			return STRING_TRUTH;
		} else if (TypeEnum.NUMERIC.equals(type)) {
			return STRING_NUMERIC;
		} else if (TypeEnum.STRING.equals(type)) {
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
	public static TypeEnum stringToType(String string) {
		if (string.equals(STRING_TRUTH)) {
			return TypeEnum.BOOLEAN;
		} else if (string.equals(STRING_NUMERIC)) {
			return TypeEnum.NUMERIC;
		} else if (string.equals(STRING_STRING)) {
			return TypeEnum.STRING;
		}
		return null;
	}
	
	/**
	 * 共通設定種別の一覧をMap形式で返します。
	 * @return 種別一覧
	 */
	public static LinkedHashMap<TypeEnum, String> getList() {
		LinkedHashMap<TypeEnum, String> map = new LinkedHashMap<TypeEnum, String>();
		
		map.put(TypeEnum.STRING, STRING_STRING);
		map.put(TypeEnum.NUMERIC, STRING_NUMERIC);
		map.put(TypeEnum.BOOLEAN, STRING_TRUTH);
		
		return map;
	}
}