/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.util;

import com.clustercontrol.notify.bean.SyslogFacilityConstant;

/**
 * syslogで用いるFacilityの定義を定数として格納するクラス<BR>
 *
 */
public class SyslogFacilityUtil {
	/** kern（Enum判定用のName）。 */
	private static final String ENUM_NAME_KERN = "KERN";

	/** user（Enum判定用のName）。 */
	private static final String ENUM_NAME_USER = "USER";

	/** mail（Enum判定用のName）。 */
	private static final String ENUM_NAME_MAIL = "MAIL";

	/** daemon（Enum判定用のName）。 */
	private static final String ENUM_NAME_DAEMON = "DAEMON";

	/** auth（Enum判定用のName）。 */
	private static final String ENUM_NAME_AUTH = "AUTH";

	/** syslog（Enum判定用のName）。 */
	private static final String ENUM_NAME_SYSLOG = "SYSLOG";

	/** lpr（Enum判定用のName）。 */
	private static final String ENUM_NAME_LPR = "LPR";

	/** news（Enum判定用のName）。 */
	private static final String ENUM_NAME_NEWS = "NEWS";

	/** uucp（Enum判定用のName）。 */
	private static final String ENUM_NAME_UUCP = "UUCP";

	/** cron（Enum判定用のName）。 */
	private static final String ENUM_NAME_CRON = "CRON";

	/** authpriv（Enum判定用のName）。 */
	private static final String ENUM_NAME_AUTHPRIV = "AUTHPRIV";

	/** ftp（Enum判定用のName）。 */
	private static final String ENUM_NAME_FTP = "FTP";

	/** local0（Enum判定用のName）。 */
	private static final String ENUM_NAME_LOCAL0 = "LOCAL0";

	/** local1（Enum判定用のName）。 */
	private static final String ENUM_NAME_LOCAL1 = "LOCAL1";

	/** local2（Enum判定用のName）。 */
	private static final String ENUM_NAME_LOCAL2 = "LOCAL2";

	/** local3（Enum判定用のName）。 */
	private static final String ENUM_NAME_LOCAL3 = "LOCAL3";

	/** local4（Enum判定用のName）。 */
	private static final String ENUM_NAME_LOCAL4 = "LOCAL4";

	/** local5（Enum判定用のName）。 */
	private static final String ENUM_NAME_LOCAL5 = "LOCAL5";

	/** local6（Enum判定用のName）。 */
	private static final String ENUM_NAME_LOCAL6 = "LOCAL6";

	/** local7（Enum判定用のName）。 */
	private static final String ENUM_NAME_LOCAL7 = "LOCAL7";


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
		if (name.equals(ENUM_NAME_KERN)) {
			return SyslogFacilityConstant.STRING_KERN;
		} else if (name.equals(ENUM_NAME_USER)) {
			return SyslogFacilityConstant.STRING_USER;
		} else if (name.equals(ENUM_NAME_MAIL)) {
			return SyslogFacilityConstant.STRING_MAIL;
		} else if (name.equals(ENUM_NAME_DAEMON)) {
			return SyslogFacilityConstant.STRING_DAEMON;
		} else if (name.equals(ENUM_NAME_AUTH)) {
			return SyslogFacilityConstant.STRING_AUTH;
		} else if (name.equals(ENUM_NAME_SYSLOG)) {
			return SyslogFacilityConstant.STRING_SYSLOG;
		} else if (name.equals(ENUM_NAME_LPR)) {
			return SyslogFacilityConstant.STRING_LPR;
		} else if (name.equals(ENUM_NAME_NEWS)) {
			return SyslogFacilityConstant.STRING_NEWS;
		} else if (name.equals(ENUM_NAME_UUCP)) {
			return SyslogFacilityConstant.STRING_UUCP;
		} else if (name.equals(ENUM_NAME_CRON)) {
			return SyslogFacilityConstant.STRING_CRON;
		} else if (name.equals(ENUM_NAME_AUTHPRIV)) {
			return SyslogFacilityConstant.STRING_AUTHPRIV;
		} else if (name.equals(ENUM_NAME_FTP)) {
			return SyslogFacilityConstant.STRING_FTP;
		} else if (name.equals(ENUM_NAME_LOCAL0)) {
			return SyslogFacilityConstant.STRING_LOCAL0;
		} else if (name.equals(ENUM_NAME_LOCAL1)) {
			return SyslogFacilityConstant.STRING_LOCAL1;
		} else if (name.equals(ENUM_NAME_LOCAL2)) {
			return SyslogFacilityConstant.STRING_LOCAL2;
		} else if (name.equals(ENUM_NAME_LOCAL3)) {
			return SyslogFacilityConstant.STRING_LOCAL3;
		} else if (name.equals(ENUM_NAME_LOCAL4)) {
			return SyslogFacilityConstant.STRING_LOCAL4;
		} else if (name.equals(ENUM_NAME_LOCAL5)) {
			return SyslogFacilityConstant.STRING_LOCAL5;
		} else if (name.equals(ENUM_NAME_LOCAL6)) {
			return SyslogFacilityConstant.STRING_LOCAL6;
		} else if (name.equals(ENUM_NAME_LOCAL7)) {
			return SyslogFacilityConstant.STRING_LOCAL7;
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
		if (string.equals(SyslogFacilityConstant.STRING_KERN)) {
			name = ENUM_NAME_KERN;
		} else if (string.equals(SyslogFacilityConstant.STRING_USER)) {
			name = ENUM_NAME_USER;
		} else if (string.equals(SyslogFacilityConstant.STRING_MAIL)) {
			name = ENUM_NAME_MAIL;
		} else if (string.equals(SyslogFacilityConstant.STRING_DAEMON)) {
			name = ENUM_NAME_DAEMON;
		} else if (string.equals(SyslogFacilityConstant.STRING_AUTH)) {
			name = ENUM_NAME_AUTH;
		} else if (string.equals(SyslogFacilityConstant.STRING_SYSLOG)) {
			name = ENUM_NAME_SYSLOG;
		} else if (string.equals(SyslogFacilityConstant.STRING_LPR)) {
			name = ENUM_NAME_LPR;
		} else if (string.equals(SyslogFacilityConstant.STRING_NEWS)) {
			name = ENUM_NAME_NEWS;
		} else if (string.equals(SyslogFacilityConstant.STRING_UUCP)) {
			name = ENUM_NAME_UUCP;
		} else if (string.equals(SyslogFacilityConstant.STRING_CRON)) {
			name = ENUM_NAME_CRON;
		} else if (string.equals(SyslogFacilityConstant.STRING_AUTHPRIV)) {
			name = ENUM_NAME_AUTHPRIV;
		} else if (string.equals(SyslogFacilityConstant.STRING_FTP)) {
			name = ENUM_NAME_FTP;
		} else if (string.equals(SyslogFacilityConstant.STRING_LOCAL0)) {
			name = ENUM_NAME_LOCAL0;
		} else if (string.equals(SyslogFacilityConstant.STRING_LOCAL1)) {
			name = ENUM_NAME_LOCAL1;
		} else if (string.equals(SyslogFacilityConstant.STRING_LOCAL2)) {
			name = ENUM_NAME_LOCAL2;
		} else if (string.equals(SyslogFacilityConstant.STRING_LOCAL3)) {
			name = ENUM_NAME_LOCAL3;
		} else if (string.equals(SyslogFacilityConstant.STRING_LOCAL4)) {
			name = ENUM_NAME_LOCAL4;
		} else if (string.equals(SyslogFacilityConstant.STRING_LOCAL5)) {
			name = ENUM_NAME_LOCAL5;
		} else if (string.equals(SyslogFacilityConstant.STRING_LOCAL6)) {
			name = ENUM_NAME_LOCAL6;
		} else if (string.equals(SyslogFacilityConstant.STRING_LOCAL7)) {
			name = ENUM_NAME_LOCAL7;
		} else {
			return null;
		}
		return Enum.valueOf(enumType, name);
	}
}
