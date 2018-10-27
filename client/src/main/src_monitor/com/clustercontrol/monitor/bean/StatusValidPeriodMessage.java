/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import com.clustercontrol.util.Messages;

/**
 * ステータス情報の存続期間の定義を定数として格納するクラスです。
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class StatusValidPeriodMessage {

	/** 無期限（文字列）。 */
	public static final String STRING_UNLIMITED = Messages.getString("unlimited");

	/** 10分（文字列）。 */
	public static final String STRING_MIN_10 = StatusValidPeriodConstant.TYPE_MIN_10 + Messages.getString("minute");

	/** 20分（文字列）。 */
	public static final String STRING_MIN_20 = StatusValidPeriodConstant.TYPE_MIN_20 + Messages.getString("minute");

	/** 30分（文字列）。 */
	public static final String STRING_MIN_30 = StatusValidPeriodConstant.TYPE_MIN_30 + Messages.getString("minute");

	/** 1時間（文字列）。 */
	public static final String STRING_HOUR_1 = StatusValidPeriodConstant.TYPE_HOUR_1/60 + Messages.getString("time.period");

	/** 3時間（文字列）。 */
	public static final String STRING_HOUR_3 = StatusValidPeriodConstant.TYPE_HOUR_3/60 + Messages.getString("time.period");

	/** 6時間（文字列）。 */
	public static final String STRING_HOUR_6 = StatusValidPeriodConstant.TYPE_HOUR_6/60 + Messages.getString("time.period");

	/** 12時間（文字列）。 */
	public static final String STRING_HOUR_12 = StatusValidPeriodConstant.TYPE_HOUR_12/60 + Messages.getString("time.period");

	/** 1日（文字列）。 */
	public static final String STRING_DAY_1 = StatusValidPeriodConstant.TYPE_DAY_1/60/24 + Messages.getString("monthday");


	/**
	 * 種別から文字列に変換します。
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == StatusValidPeriodConstant.TYPE_UNLIMITED) {
			return STRING_UNLIMITED;
		} else if (type == StatusValidPeriodConstant.TYPE_MIN_10) {
			return STRING_MIN_10;
		} else if (type == StatusValidPeriodConstant.TYPE_MIN_20) {
			return STRING_MIN_20;
		} else if (type == StatusValidPeriodConstant.TYPE_MIN_30) {
			return STRING_MIN_30;
		} else if (type == StatusValidPeriodConstant.TYPE_HOUR_1) {
			return STRING_HOUR_1;
		} else if (type == StatusValidPeriodConstant.TYPE_HOUR_3) {
			return STRING_HOUR_3;
		} else if (type == StatusValidPeriodConstant.TYPE_HOUR_6) {
			return STRING_HOUR_6;
		} else if (type == StatusValidPeriodConstant.TYPE_HOUR_12) {
			return STRING_HOUR_12;
		} else if (type == StatusValidPeriodConstant.TYPE_DAY_1) {
			return STRING_DAY_1;
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
		if (string.equals(STRING_UNLIMITED)) {
			return StatusValidPeriodConstant.TYPE_UNLIMITED;
		} else if (string.equals(STRING_MIN_10)) {
			return StatusValidPeriodConstant.TYPE_MIN_10;
		} else if (string.equals(STRING_MIN_20)) {
			return StatusValidPeriodConstant.TYPE_MIN_20;
		} else if (string.equals(STRING_MIN_30)) {
			return StatusValidPeriodConstant.TYPE_MIN_30;
		} else if (string.equals(STRING_HOUR_1)) {
			return StatusValidPeriodConstant.TYPE_HOUR_1;
		} else if (string.equals(STRING_HOUR_3)) {
			return StatusValidPeriodConstant.TYPE_HOUR_3;
		} else if (string.equals(STRING_HOUR_6)) {
			return StatusValidPeriodConstant.TYPE_HOUR_6;
		} else if (string.equals(STRING_HOUR_12)) {
			return StatusValidPeriodConstant.TYPE_HOUR_12;
		} else if (string.equals(STRING_DAY_1)) {
			return StatusValidPeriodConstant.TYPE_DAY_1;
		}
		return -1;
	}
}