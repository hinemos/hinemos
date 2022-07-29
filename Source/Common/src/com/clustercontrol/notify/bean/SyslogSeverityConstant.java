/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.bean;

/**
 * syslogで用いるSeverityの定数クラス<BR>
 * 
 * @version 3.0.0
 * @since 3.0.0
 */
public class SyslogSeverityConstant {
	/** EMERGENCY（種別）。 */
	public static final int TYPE_EMERG = 0;

	/** ALERT（種別）。 */
	public static final int TYPE_ALERT = 1;

	/** CRITICAL（種別）。 */
	public static final int TYPE_CRIT = 2;

	/** ERROR（種別）。 */
	public static final int TYPE_ERR = 3;

	/** WARNING（種別）。 */
	public static final int TYPE_WARNING = 4;

	/** NOTICE（種別）。 */
	public static final int TYPE_NOTICE = 5;

	/** INFOMATION（種別）。 */
	public static final int TYPE_INFO = 6;

	/** DEBUG（種別）。 */
	public static final int TYPE_DEBUG = 7;


	/** EMERGENCY（文字列）。 */
	public static final String STRING_EMERG = "emergency";

	/** ALERT（文字列）。 */
	public static final String STRING_ALERT = "alert";

	/** CRITICAL（文字列）。 */
	public static final String STRING_CRIT = "critical";

	/** ERROR（文字列）。 */
	public static final String STRING_ERR = "error";

	/** WARNING（文字列）。 */
	public static final String STRING_WARNING = "warning";

	/** NOTICE（文字列）。 */
	public static final String STRING_NOTICE = "notice";

	/** INFOMATION（文字列）。 */
	public static final String STRING_INFO = "information";

	/** DEBUG（文字列）。 */
	public static final String STRING_DEBUG = "debug";


	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == TYPE_EMERG) {
			return STRING_EMERG;
		} else if (type == TYPE_ALERT) {
			return STRING_ALERT;
		} else if (type == TYPE_CRIT) {
			return STRING_CRIT;
		} else if (type == TYPE_ERR) {
			return STRING_ERR;
		} else if (type == TYPE_WARNING) {
			return STRING_WARNING;
		} else if (type == TYPE_NOTICE) {
			return STRING_NOTICE;
		} else if (type == TYPE_INFO) {
			return STRING_INFO;
		} else if (type == TYPE_DEBUG) {
			return STRING_DEBUG;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param stirng 文字列
	 * @return 種別
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_EMERG)) {
			return TYPE_EMERG;
		} else if (string.equals(STRING_ALERT)) {
			return TYPE_ALERT;
		} else if (string.equals(STRING_CRIT)) {
			return TYPE_CRIT;
		} else if (string.equals(STRING_ERR)) {
			return TYPE_ERR;
		} else if (string.equals(STRING_WARNING)) {
			return TYPE_WARNING;
		} else if (string.equals(STRING_NOTICE)) {
			return TYPE_NOTICE;
		} else if (string.equals(STRING_INFO)) {
			return TYPE_INFO;
		} else if (string.equals(STRING_DEBUG)) {
			return TYPE_DEBUG;
		}
		return -1;
	}
}