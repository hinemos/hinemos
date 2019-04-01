/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JSONデータを扱うためのUtil<br>
 * <br>
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
public class JsonUtil {

	// ログ出力関連.
	/** ロガー */
	private static Log log = LogFactory.getLog(JsonUtil.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * 単純な項目をJSON形式の文字列に変換.<br>
	 * <br>
	 * 
	 * @param fieldName
	 *            項目名
	 * @param value
	 *            項目の値(nullもOK)
	 * @return JSON形式の文字列<br>
	 *         ex. 通常) "fieldName":"value"<br>
	 *         ex. fieldNameがnull) null<br>
	 *         ex. valueがnull) "fieldName":null<br>
	 */
	public static String simpleToString(String fieldName, String value) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		if (fieldName == null) {
			message = "the necessary argument 'fieldName' is null.";
			log.warn(methodName + DELIMITER + message);
			return "null";
		}

		String returnStr = "\"" + fieldName + "\":";
		if (value == null) {
			returnStr = returnStr + "null";
			return returnStr;

		}

		returnStr = returnStr + "\"" + value + "\"";
		return returnStr;

	}

	/**
	 * リスト型の項目をJSON形式の文字列に変換.<br>
	 * <br>
	 * 
	 * @param fieldName
	 *            項目名
	 * @param valueList
	 *            リスト項目(toStringメソッドで各項目の値が取得できること)
	 * @return JSON形式の文字列<br>
	 *         ex. 通常) "fieldName":["value1","value2",...]<br>
	 *         ex. fieldNameがnull) null<br>
	 *         ex. valueがnull) "fieldName":[null,null,...]<br>
	 */
	public static <T> String listToString(String fieldName, List<T> valueList) {
		return listToString(fieldName, valueList, true);
	}

	/**
	 * リスト型の項目をJSON形式の文字列に変換.<br>
	 * <br>
	 * 
	 * @param fieldName
	 *            項目名
	 * @param valueList
	 *            リスト項目(toStringメソッドで各項目の値が取得できること)
	 * @param notNest
	 *            nestでBeanではなくvalueListに個々の項目が入っているか、true:個々の値、false:Bean
	 * @return JSON形式の文字列<br>
	 *         ex. 通常nest=true) "fieldName":["value1","value2",...]<br>
	 *         ex. 通常nest=false) "fieldName":[bean1,bean2,...]<br>
	 *         ex. fieldNameがnull) null<br>
	 *         ex. valueがnull) "fieldName":[null,null,...]<br>
	 */
	public static <T> String listToString(String fieldName, List<T> valueList, boolean notNest) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		if (fieldName == null) {
			message = "the necessary argument 'fieldName' is null.";
			log.warn(methodName + DELIMITER + message);
			return "null";
		}
		StringBuilder jsonSb = new StringBuilder();

		jsonSb.append("\"" + fieldName + "\":");
		if (valueList == null || valueList.isEmpty()) {
			jsonSb.append("null");
			return jsonSb.toString();
		}

		jsonSb.append("[");
		boolean first = true;
		for (T value : valueList) {
			if (!first) {
				jsonSb.append(",");
			}
			if (value == null) {
				jsonSb.append("null");
				continue;
			}

			if (notNest) {
				jsonSb.append("\"");
			}
			jsonSb.append(value.toString());
			if (notNest) {
				jsonSb.append("\"");
			}
			first = false;
		}
		jsonSb.append("]");

		return jsonSb.toString();
	}
}
