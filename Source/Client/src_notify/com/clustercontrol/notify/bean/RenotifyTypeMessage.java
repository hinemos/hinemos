/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.bean;

import com.clustercontrol.util.Messages;

/**
 * 抑制間隔の定義を定数として格納するクラス<BR>
 *
 * @version 2.1.0
 * @since 1.0.0
 */
public class RenotifyTypeMessage {
	/** 常に通知する（文字列）。 */
	public static final String STRING_ALWAYS_NOTIFY = Messages.getString("suppress.no");

	/** 期間で抑制する（分単位）（文字列）。 */
	public static final String STRING_PERIOD = Messages.getString("suppress.by.time.interval");

	/** 再通知しない（文字列）。 */
	public static final String STRING_NO_NOTIFY = Messages.getString("suppress.always");

	/** 常に通知する（Enum判定用のName）。 */
	private static final String ENUM_NAME_ALWAYS_NOTIFY = "ALWAYS_NOTIFY";

	/** 期間で抑制する（分単位）（Enum判定用のName）。 */
	private static final String ENUM_NAME_PERIOD = "PERIOD";

	/** 再通知しない（Enum判定用のName）。 */
	private static final String ENUM_NAME_NO_NOTIFY = "NO_NOTIFY";


	/**
	 * 種別から文字列に変換します。<BR>
	 *
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == RenotifyTypeConstant.TYPE_ALWAYS_NOTIFY) {
			return STRING_ALWAYS_NOTIFY;
		} else if (type == RenotifyTypeConstant.TYPE_PERIOD) {
			return STRING_PERIOD;
		} else if (type == RenotifyTypeConstant.TYPE_NO_NOTIFY) {
			return STRING_NO_NOTIFY;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * @param string 文字列
	 * @return 種別
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_ALWAYS_NOTIFY)) {
			return RenotifyTypeConstant.TYPE_ALWAYS_NOTIFY;
		} else if (string.equals(STRING_PERIOD)) {
			return RenotifyTypeConstant.TYPE_PERIOD;
		} else if (string.equals(STRING_NO_NOTIFY)) {
			return RenotifyTypeConstant.TYPE_NO_NOTIFY;
		}
		return -1;
	}

	/**
	 * 種別からEnumに変換します。<BR>
	 * ※Enumの型は引数で指定できますが、列挙子のNameは統一されている必要があります。<BR>
	 * 
	 * @param type 種別
	 * @param enumType Enumの型
	 * @return Enum
	 */
	public static <T extends Enum<T>> T typeToEnum(int type, Class<T> enumType) {
		String name = "";
		if (type == RenotifyTypeConstant.TYPE_ALWAYS_NOTIFY) {
			name = ENUM_NAME_ALWAYS_NOTIFY;
		} else if (type == RenotifyTypeConstant.TYPE_PERIOD) {
			name = ENUM_NAME_PERIOD;
		} else if (type == RenotifyTypeConstant.TYPE_NO_NOTIFY) {
			name = ENUM_NAME_NO_NOTIFY;
		} else {
			return null;
		}
		return Enum.valueOf(enumType, name);
	}

	/**
	 * Enumから種別に変換します。<BR>
	 * ※Enumの型は引数で指定できますが、列挙子のNameは統一されている必要があります。<BR>
	 * 
	 * @param value 変換するEnum
	 * @param enumType Enumの型
	 * @return 種別
	 */
	public static <T extends Enum<T>> int enumToType(T value, Class<T> enumType) {
		String name = value.name();
		if (name.equals(ENUM_NAME_ALWAYS_NOTIFY)) {
			return RenotifyTypeConstant.TYPE_ALWAYS_NOTIFY;
		} else if (name.equals(ENUM_NAME_PERIOD)) {
			return RenotifyTypeConstant.TYPE_PERIOD;
		} else if (name.equals(ENUM_NAME_NO_NOTIFY)) {
			return RenotifyTypeConstant.TYPE_NO_NOTIFY;
		}
		return -1;
	}
}