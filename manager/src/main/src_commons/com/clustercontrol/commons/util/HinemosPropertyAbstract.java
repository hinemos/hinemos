/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import com.clustercontrol.commons.bean.HinemosPropertyBean;
import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.maintenance.model.HinemosPropertyInfo;
import com.clustercontrol.maintenance.session.HinemosPropertyControllerBean;

/**
 * HinemosPropertyのデフォルト値を格納するインタフェース<BR>
 * 実装先はEnumを想定
 *
 * @version 6.1.0
 */
public interface HinemosPropertyAbstract {

	// Hinemosプロパティ情報取得
	HinemosPropertyBean getBean();

	// Hinemosプロパティ名取得
	String name();

	/**
	 * キー値変換
	 * 名前を"_"から"."に変換
	 * 例）　COMMON_TIME_OFFSET → common.time.offset
	 * @return 変換後のキー
	 */
	default String getKey() {
		return this.name().replace('_', '.');
	}

	/**
	 * キー値変換
	 * 名前を"_"から"."に変換
	 * $は置換文字列に変換
	 * 例）　COMMON_$_OFFSET、置換文字列"time" → common.time.offset
	 * 
	 * @param replaceStr 置換文字列
	 * @return 変換後のキー
	 */
	default String getReplaceKey(String replaceStr) {
		return getKey().replace("$", replaceStr);
	}

	/**
	 * Hinemosプロパティ（文字列）取得
	 * 存在しない場合はデフォルト値を使用
	 * 
	 * @return Hinemosプロパティ値
	 */
	default String getStringValue() throws NullPointerException {
		HinemosPropertyBean bean = getBean();
		if (bean.getType() != HinemosPropertyTypeConstant.TYPE_STRING) {
			throw new NullPointerException();
		}
		HinemosPropertyInfo info = new HinemosPropertyControllerBean().getHinemosPropertyInfo_None(getKey());
		if (info == null 
				|| info.getValueType() != HinemosPropertyTypeConstant.TYPE_STRING
				|| info.getValueString() == null 
				|| info.getValueString().isEmpty()) {
			return bean.getDefaultStringValue();
		}
		return info.getValueString();
	}

	/**
	 * Hinemosプロパティ（文字列）取得
	 * 存在しない場合はデフォルト値を使用
	 * Hinemosプロパティ名の"$"を、指定された置換文字列で置換する。
	 * 
	 * @param replaceStr　置換文字列
	 * @param defaultValue　デフォルト値
	 * @return Hinemosプロパティ値
	 * @throws NullPointerException
	 */
	default String getStringValue(String replaceStr, String defaultValue) throws NullPointerException {
		HinemosPropertyBean bean = getBean();
		if (bean.getType() != HinemosPropertyTypeConstant.TYPE_STRING) {
			throw new NullPointerException();
		}
		HinemosPropertyInfo info = new HinemosPropertyControllerBean().getHinemosPropertyInfo_None(getReplaceKey(replaceStr));
		if (info == null 
				|| info.getValueType() != HinemosPropertyTypeConstant.TYPE_STRING
				|| info.getValueString() == null 
				|| info.getValueString().isEmpty()) {
			return defaultValue;
		}
		return info.getValueString();
	}

	/**
	 * Hinemosプロパティ（数値）取得
	 * 存在しない場合はデフォルト値を使用
	 * 
	 * @return Hinemosプロパティ値
	 */
	default Long getNumericValue() throws NullPointerException {
		HinemosPropertyBean bean = getBean();
		if (bean.getType() != HinemosPropertyTypeConstant.TYPE_NUMERIC) {
			throw new NullPointerException();
		}
		HinemosPropertyInfo info = new HinemosPropertyControllerBean().getHinemosPropertyInfo_None(getKey());
		if (info == null 
				|| info.getValueType() != HinemosPropertyTypeConstant.TYPE_NUMERIC
				|| info.getValueNumeric() == null) {
			return bean.getDefaultNumericValue();
		}
		return info.getValueNumeric();
	}

	/**
	 * Hinemosプロパティ（数値）取得
	 * 存在しない場合はデフォルト値を使用
	 * Hinemosプロパティ名の"$"を、指定された置換文字列で置換する。
	 * 
	 * @param replaceStr　置換文字列
	 * @param defaultValue　デフォルト値
	 * @return Hinemosプロパティ値
	 * @throws NullPointerException
	 */
	default Long getNumericValue(String replaceStr, Long defaultValue) throws NullPointerException {
		HinemosPropertyBean bean = getBean();
		if (bean.getType() != HinemosPropertyTypeConstant.TYPE_NUMERIC) {
			throw new NullPointerException();
		}
		HinemosPropertyInfo info = new HinemosPropertyControllerBean().getHinemosPropertyInfo_None(getReplaceKey(replaceStr));
		if (info == null 
				|| info.getValueType() != HinemosPropertyTypeConstant.TYPE_NUMERIC
				|| info.getValueNumeric() == null) {
			return defaultValue;
		}
		return info.getValueNumeric();
	}

	/**
	 * Hinemosプロパティ（Integer）取得
	 * 存在しない場合はデフォルト値を使用
	 * Integerの最大値を超過した値の場合は、最大値を返す。
	 * 
	 * @return Hinemosプロパティ値
	 */
	default Integer getIntegerValue() throws NullPointerException {
		Long longValue = getNumericValue();
		if (longValue == null) {
			return null;
		} else if (longValue.compareTo(Long.valueOf(Integer.MAX_VALUE)) > 0) {
			return Integer.MAX_VALUE;
		} else {
			return longValue.intValue();
		}
	}

	/**
	 * Hinemosプロパティ（Integer）取得
	 * 存在しない場合はデフォルト値を使用
	 * Hinemosプロパティ名の"$"を、指定された置換文字列で置換する。
	 * Integerの最大値を超過した値の場合は、最大値を返す。
	 * 
	 * @param replaceStr　置換文字列
	 * @param defaultValue　デフォルト値
	 * @return Hinemosプロパティ値
	 * @throws NullPointerException
	 */
	default Integer getIntegerValue(String replaceStr, Long defaultValue) throws NullPointerException {
		Long longValue = getNumericValue(replaceStr, defaultValue);
		if (longValue == null) {
			return null;
		} else if (longValue.compareTo(Long.valueOf(Integer.MAX_VALUE)) > 0) {
			return Integer.MAX_VALUE;
		} else {
			return longValue.intValue();
		}
	}

	/**
	 * Hinemosプロパティ（真偽値）取得
	 * 存在しない場合はデフォルト値を使用
	 * 
	 * @return Hinemosプロパティ値
	 */
	default Boolean getBooleanValue() throws NullPointerException {
		HinemosPropertyBean bean = getBean();
		if (bean.getType() != HinemosPropertyTypeConstant.TYPE_TRUTH) {
			throw new NullPointerException();
		}
		HinemosPropertyInfo info = new HinemosPropertyControllerBean().getHinemosPropertyInfo_None(getKey());
		if (info == null 
				|| info.getValueType() != HinemosPropertyTypeConstant.TYPE_TRUTH
				|| info.getValueBoolean() == null) {
			return bean.getDefaultBooleanValue();
		}
		return info.getValueBoolean();
	}

	/**
	 * Hinemosプロパティ（真偽値）取得
	 * 存在しない場合はデフォルト値を使用
	 * Hinemosプロパティ名の"$"を、指定された置換文字列で置換する。
	 * 
	 * @param replaceStr　置換文字列
	 * @param defaultValue　デフォルト値
	 * @return Hinemosプロパティ値
	 * @throws NullPointerException
	 */
	default Boolean getBooleanValue(String replaceStr, Boolean defaultValue) throws NullPointerException {
		HinemosPropertyBean bean = getBean();
		if (bean.getType() != HinemosPropertyTypeConstant.TYPE_TRUTH) {
			throw new NullPointerException();
		}
		HinemosPropertyInfo info = new HinemosPropertyControllerBean().getHinemosPropertyInfo_None(getReplaceKey(replaceStr));
		if (info == null 
				|| info.getValueType() != HinemosPropertyTypeConstant.TYPE_TRUTH
				|| info.getValueBoolean() == null) {
			return defaultValue;
		}
		return info.getValueBoolean();
	}

	/**
	 * エラーメッセージ
	 * 
	 * @param value 値
	 * @return エラーメッセージ
	 */
	default String message_invalid(String value) {
		HinemosPropertyBean bean = getBean();
		String defaultValue = "";
		if (bean.getType() == HinemosPropertyTypeConstant.TYPE_STRING) {
			defaultValue = bean.getDefaultStringValue();
		} else if (bean.getType() == HinemosPropertyTypeConstant.TYPE_NUMERIC) {
			defaultValue = bean.getDefaultNumericValue().toString();
		} else if (bean.getType() == HinemosPropertyTypeConstant.TYPE_TRUTH) {
			defaultValue = bean.getDefaultBooleanValue().toString();
		}
		return String.format("key=%s, value=%s, invalid value then using %s", getKey(), value, defaultValue);
	}
}

