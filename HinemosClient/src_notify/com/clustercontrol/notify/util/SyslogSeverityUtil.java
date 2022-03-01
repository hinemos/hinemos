/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.util;

import com.clustercontrol.notify.bean.SyslogSeverityConstant;

/**
 * syslogで用いるSeverityの定義を定数として格納するクラス<BR>
 *
 */
public class SyslogSeverityUtil {
	/** EMERGENCY（Enum判定用のName）。 */
	private static final String ENUM_NAME_EMERG = "EMERGENCY";

	/** ALERT（Enum判定用のName）。 */
	private static final String ENUM_NAME_ALERT = "ALERT";

	/** CRITICAL（Enum判定用のName）。 */
	private static final String ENUM_NAME_CRIT = "CRITICAL";

	/** ERROR（Enum判定用のName）。 */
	private static final String ENUM_NAME_ERR = "ERROR";

	/** WARNING（Enum判定用のName）。 */
	private static final String ENUM_NAME_WARNING = "WARNING";

	/** NOTICE（Enum判定用のName）。 */
	private static final String ENUM_NAME_NOTICE = "NOTICE";

	/** INFOMATION（Enum判定用のName）。 */
	private static final String ENUM_NAME_INFO = "INFOMATION";

	/** DEBUG（Enum判定用のName）。 */
	private static final String ENUM_NAME_DEBUG = "DEBUG";


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
		if (name.equals(ENUM_NAME_EMERG)) {
			return SyslogSeverityConstant.STRING_EMERG;
		} else if (name.equals(ENUM_NAME_ALERT)) {
			return SyslogSeverityConstant.STRING_ALERT;
		} else if (name.equals(ENUM_NAME_CRIT)) {
			return SyslogSeverityConstant.STRING_CRIT;
		} else if (name.equals(ENUM_NAME_ERR)) {
			return SyslogSeverityConstant.STRING_ERR;
		} else if (name.equals(ENUM_NAME_WARNING)) {
			return SyslogSeverityConstant.STRING_WARNING;
		} else if (name.equals(ENUM_NAME_NOTICE)) {
			return SyslogSeverityConstant.STRING_NOTICE;
		} else if (name.equals(ENUM_NAME_INFO)) {
			return SyslogSeverityConstant.STRING_INFO;
		} else if (name.equals(ENUM_NAME_DEBUG)) {
			return SyslogSeverityConstant.STRING_DEBUG;
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
		if (string.equals(SyslogSeverityConstant.STRING_EMERG)) {
			name = ENUM_NAME_EMERG;
		} else if (string.equals(SyslogSeverityConstant.STRING_ALERT)) {
			name = ENUM_NAME_ALERT;
		} else if (string.equals(SyslogSeverityConstant.STRING_CRIT)) {
			name = ENUM_NAME_CRIT;
		} else if (string.equals(SyslogSeverityConstant.STRING_ERR)) {
			name = ENUM_NAME_ERR;
		} else if (string.equals(SyslogSeverityConstant.STRING_WARNING)) {
			name = ENUM_NAME_WARNING;
		} else if (string.equals(SyslogSeverityConstant.STRING_NOTICE)) {
			name = ENUM_NAME_NOTICE;
		} else if (string.equals(SyslogSeverityConstant.STRING_INFO)) {
			name = ENUM_NAME_INFO;
		} else if (string.equals(SyslogSeverityConstant.STRING_DEBUG)) {
			name = ENUM_NAME_DEBUG;
		} else {
			return null;
		}
		return Enum.valueOf(enumType, name);
	}
}
