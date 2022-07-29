/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.util.Locale;

import org.openapitools.client.model.MonitorNumericValueInfoRequest;

import com.clustercontrol.util.Messages;

/**
 * 重要度の定義を定数として格納するクラス<BR>
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
public class PriorityMessage {
	/** 危険（文字列）。 */
	public static final String STRING_CRITICAL = Messages.getString("critical");

	/** 警告（文字列）。 */
	public static final String STRING_WARNING = Messages.getString("warning");

	/** 通知（文字列）。 */
	public static final String STRING_INFO = Messages.getString("info");

	/** 不明（文字列）。 */
	public static final String STRING_UNKNOWN = Messages.getString("unknown");

	/** 危険 日本語（文字列）。 */
	public static final String STRING_JP_CRITICAL = Messages.getString("critical", Locale.JAPANESE);

	/** 警告 日本語（文字列）。 */
	public static final String STRING_JP_WARNING = Messages.getString("warning", Locale.JAPANESE);

	/** 通知 日本語（文字列）。 */
	public static final String STRING_JP_INFO = Messages.getString("info", Locale.JAPANESE);

	/** 不明 日本語（文字列）。 */
	public static final String STRING_JP_UNKNOWN = Messages.getString("unknown", Locale.JAPANESE);

	/** 危険 英語（文字列）。 */
	public static final String STRING_EN_CRITICAL = Messages.getString("critical", Locale.ENGLISH);

	/** 警告 英語（文字列）。 */
	public static final String STRING_EN_WARNING = Messages.getString("warning", Locale.ENGLISH);

	/** 通知 英語（文字列）。 */
	public static final String STRING_EN_INFO = Messages.getString("info", Locale.ENGLISH);

	/** 不明 英語（文字列）。 */
	public static final String STRING_EN_UNKNOWN = Messages.getString("unknown", Locale.ENGLISH);

	/** なし（文字列）。 */
	public static final String STRING_NONE = "";

	/** 危険 Enum判定用のName */
	private static final String ENUM_NAME_CRITICAL = "CRITICAL";

	/** 警告 Enum判定用のName */
	private static final String ENUM_NAME_WARNING = "WARNING";

	/** 通知 Enum判定用のName */
	private static final String ENUM_NAME_INFO = "INFO";

	/** 不明 Enum判定用のName */
	private static final String ENUM_NAME_UNKNOWN = "UNKNOWN";

	/** なし Enum判定用のName */
	private static final String ENUM_NAME_NONE = "NONE";
	
	/** 重要度のリスト（重要度の高いもの順） **/
	public static int[] PRIORITY_LIST = {
		PriorityConstant.TYPE_CRITICAL,
		PriorityConstant.TYPE_UNKNOWN,
		PriorityConstant.TYPE_WARNING,
		PriorityConstant.TYPE_INFO,
	};

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == PriorityConstant.TYPE_CRITICAL) {
			return STRING_CRITICAL;
		} else if (type == PriorityConstant.TYPE_WARNING) {
			return STRING_WARNING;
		} else if (type == PriorityConstant.TYPE_INFO) {
			return STRING_INFO;
		} else if (type == PriorityConstant.TYPE_UNKNOWN) {
			return STRING_UNKNOWN;
		} else if (type == PriorityConstant.TYPE_NONE) {
			return STRING_NONE;
		}
		return "";
	}

	/**
	 * 種別から文字列（日本語）に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToStringJP(int type) {
		if (type == PriorityConstant.TYPE_CRITICAL) {
			return STRING_JP_CRITICAL;
		} else if (type == PriorityConstant.TYPE_WARNING) {
			return STRING_JP_WARNING;
		} else if (type == PriorityConstant.TYPE_INFO) {
			return STRING_JP_INFO;
		} else if (type == PriorityConstant.TYPE_UNKNOWN) {
			return STRING_JP_UNKNOWN;
		} else if (type == PriorityConstant.TYPE_NONE) {
			return STRING_NONE;
		}
		return "";
	}

	/**
	 * 種別から文字列（英語）に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToStringEN(int type) {
		if (type == PriorityConstant.TYPE_CRITICAL) {
			return STRING_EN_CRITICAL;
		} else if (type == PriorityConstant.TYPE_WARNING) {
			return STRING_EN_WARNING;
		} else if (type == PriorityConstant.TYPE_INFO) {
			return STRING_EN_INFO;
		} else if (type == PriorityConstant.TYPE_UNKNOWN) {
			return STRING_EN_UNKNOWN;
		} else if (type == PriorityConstant.TYPE_NONE) {
			return STRING_NONE;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_CRITICAL)) {
			return PriorityConstant.TYPE_CRITICAL;
		} else if (string.equals(STRING_WARNING)) {
			return PriorityConstant.TYPE_WARNING;
		} else if (string.equals(STRING_INFO)) {
			return PriorityConstant.TYPE_INFO;
		} else if (string.equals(STRING_UNKNOWN)) {
			return PriorityConstant.TYPE_UNKNOWN;
		} else if (string.equals(STRING_NONE)) {
			return PriorityConstant.TYPE_NONE;
		}
		return -1;
	}
	

	/**
	 * PriorityEnumの値から文字列に変換します。<BR>
	 * 
	 * @param code PriorityEnumの値
	 * @return 文字列
	 */
	public static String codeToString(String code) {
		if (MonitorNumericValueInfoRequest.PriorityEnum.CRITICAL.getValue().equals(code)) {
			return STRING_CRITICAL;
		} else if (MonitorNumericValueInfoRequest.PriorityEnum.WARNING.getValue().equals(code)) {
			return STRING_WARNING;
		} else if (MonitorNumericValueInfoRequest.PriorityEnum.INFO.getValue().equals(code)) {
			return STRING_INFO;
		} else if (MonitorNumericValueInfoRequest.PriorityEnum.UNKNOWN.getValue().equals(code)) {
			return STRING_UNKNOWN;
		} else if (MonitorNumericValueInfoRequest.PriorityEnum.NONE.getValue().equals(code)) {
			return STRING_NONE;
		}
		return "";
	}

	/**
	 * Enumから文字列に変換します。<BR>
	 * ※Enumの型は引数で指定できますが、列挙子のNameは統一されている必要があります。<BR>
	 * 
	 * @param value 変換するEnum
	 * @param enumType Enumの型
	 * @return 文字列
	 */
	public static <T extends Enum<T>> String enumToString(T value, Class<T> enumType) {
		String name = value.name();
		if (name.equals(ENUM_NAME_CRITICAL)) {
			return STRING_CRITICAL;
		} else if (name.equals(ENUM_NAME_WARNING)) {
			return STRING_WARNING;
		} else if (name.equals(ENUM_NAME_INFO)) {
			return STRING_INFO;
		} else if (name.equals(ENUM_NAME_UNKNOWN)) {
			return STRING_UNKNOWN;
		} else if (name.equals(ENUM_NAME_NONE)) {
			return STRING_NONE;
		}
		return "";
	}

	/**
	 * 文字列からEnumに変換します。<BR>
	 * ※Enumの型は引数で指定できますが、列挙子のNameは統一されている必要があります。<BR>
	 * 
	 * @param string 文字列
	 * @param enumType Enumの型
	 * @return 種別
	 */
	public static <T extends Enum<T>> T stringToEnum(String string, Class<T> enumType) {
		String name = "";
		if (string.equals(STRING_CRITICAL)) {
			name = ENUM_NAME_CRITICAL;
		} else if (string.equals(STRING_WARNING)) {
			name = ENUM_NAME_WARNING;
		} else if (string.equals(STRING_INFO)) {
			name = ENUM_NAME_INFO;
		} else if (string.equals(STRING_UNKNOWN)) {
			name = ENUM_NAME_UNKNOWN;
		} else if (string.equals(STRING_NONE)) {
			name = ENUM_NAME_NONE;
		} else {
			return null;
		}
		return Enum.valueOf(enumType, name);
	}

	/**
	 * ジョブ通知の呼出失敗時の重要度のデフォルト値です。
	 * 
	 * @return
	 */
	public static String getNotifyJobFailurePriorityDefault() {
		return STRING_UNKNOWN;
	}

	public static int getNotifyJobFailurePriorityDefaultValue() {
		return stringToType(getNotifyJobFailurePriorityDefault());
	}

}