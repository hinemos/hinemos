/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.logging.util;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.clustercontrol.logging.constant.MessageConstant;
import com.clustercontrol.logging.exception.LoggingPropertyException;

public class CommonValidater {

	/**
	 * nullであるかをバリデーションします。
	 * 
	 */
	public static void validateRequired(String name, Object value) throws LoggingPropertyException {

		if (value == null || value.equals("")) {
			LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateEmpty(name));
			throw e;
		}
	}

	/**
	 * 指定されたIDがHinemosのID規則にマッチするかを確認する。 [a-z,A-Z,0-9,-,_,.,@]のみ許可する
	 * (Hinemos5.0で「.」と「@」を追加)
	 */
	public static void validateId(String name, String id, int maxSize) throws LoggingPropertyException {
		if (id == null || id.equals("")) {
			LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateEmpty(name));
			throw e;
		}

		// string check
		validateStringLength(name, id, false, 1, maxSize);

		/** メイン処理 */
		if (!id.matches("^[A-Za-z0-9-_.@]+$")) {
			LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateForbidden(name));
			throw e;
		}
	}

	/**
	 * 文字列の文字チェック
	 */
	public static void validateString(String name, String str, boolean required) throws LoggingPropertyException {
		if (required) {
			validateRequired(name, str);
		} else {
			if (str == null) {
				return;
			}
		}

		if (str.matches("^.*[\u0000-\u001F].*")) {
			LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateForbidden(name));
			throw e;
		}
	}

	/**
	 * 文字列の長さチェック
	 */
	public static void validateStringLength(String name, String str, boolean required, int minLength, int maxLength)
			throws LoggingPropertyException {
		validateString(name, str, required);
		if (str == null) {
			return;
		}
		int size = str.length();
		if (size < minLength) {
			if (size == 0) {
				LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateEmpty(name));
				throw e;
			} else {
				LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateStrSerect(name));
				throw e;
			}
		}

		if (size > maxLength) {
			LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateStrLong(name));
			throw e;
		}
	}

	/**
	 * 文字列の選択肢チェック
	 */

	public static void validateStringSelect(String name, String str, boolean required, String[] select)
			throws LoggingPropertyException {
		if (required) {
			validateRequired(name, str);
		} else {
			if (str == null) {
				return;
			}
		}

		if (!Arrays.asList(select).contains(str)) {
			LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateStrSerect(name));
			throw e;
		}
	}

	/**
	 * 文字列の真偽値変換チェック
	 */
	public static void validateStringToBool(String name, String str, boolean required) throws LoggingPropertyException {
		if (required) {
			validateRequired(name, str);
		} else {
			if (str == null) {
				return;
			}
		}

		if (str.equals("true") || str.equals("false")) {
			return;
		} else {
			LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateType(name));
			throw e;
		}
	}

	/**
	 * 文字列の数値変換チェック
	 */
	public static void validateStringToInteger(String name, String str, boolean required)
			throws LoggingPropertyException {
		if (required) {
			validateRequired(name, str);
		} else {
			if (str == null) {
				return;
			}
		}

		try {
			Integer.parseInt(str);
		} catch (NumberFormatException e1) {
			try {
				Long.parseLong(str);
			} catch (NumberFormatException e2) {
				LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateType(name));
				throw e;
			}
			LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateNumRange(name));
			throw e;
		}
	}

	/**
	 * 数値の上限下限チェック
	 */
	public static void validateInt(String name, Integer i, int minSize, int maxSize) throws LoggingPropertyException {
		if (i == null || i < minSize || maxSize < i) {
			LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateNumRange(name));
			throw e;
		}
	}

	/**
	 * 数値の上限下限チェック
	 */
	public static void validateNullableInt(String name, Integer i, int minSize, int maxSize)
			throws LoggingPropertyException {
		if (i == null)
			return;
		validateInt(name, i, minSize, maxSize);
	}

	/**
	 * Integerの上限下限チェック（チェックの一部スキップ可能）
	 */
	public static void validateIntegerSkippable(String name, Integer target, int minSize, int maxSize)
			throws LoggingPropertyException {
		// 対象がNULLなら検査しない
		if (target == null) {
			return;
		}
		// 最低数検査
		if (minSize != -1 && target < minSize) {
			LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateNumRange(name));

			throw e;
		}
		// 最大値検査
		if (maxSize != -1 && maxSize < target) {
			LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateNumRange(name));
			throw e;
		}
	}

	public static void validateIntIsFileSize(String name, String str, boolean required)
			throws LoggingPropertyException {
		if (required) {
			validateRequired(name, str);
		} else {
			if (str == null) {
				return;
			}
		}

		if (!str.matches("[1-9][0-9]+")) {
			String regex = "^[1-9][0-9]{0,3}(KB|MB|GB)";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(str);
			if (!m.find()) {
				LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateIntFileSize(name));
				throw e;
			}
		}

	}

	/**
	 * 文字列のカンマ分割が、有効かを確認する
	 */
	public static void validateSplitComma(String name, String str, boolean required) throws LoggingPropertyException {
		if (required) {
			validateRequired(name, str);
		} else {
			if (str == null) {
				return;
			}
		}

		int count = 0;
		for (char x : str.toCharArray()) {
			if (x == ',') {
				count++;
			}
		}
		if (!(count == 1)) {
			LoggingPropertyException e = new LoggingPropertyException(MessageConstant.getValidateFormat(name));
			throw e;
		}
	}
}
