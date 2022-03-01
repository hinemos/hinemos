/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.bean;

import org.openapitools.client.model.CalendarDetailInfoResponse.WeekNoEnum;

import com.clustercontrol.util.Messages;

/**
 * クライアントで用いる曜日の定数クラス<BR>
 */
public class DayOfWeekConstant {
	/*
	 * 曜日の名称
	 */
	/** 日曜日 */
	public static final String STRING_SUNDAY = Messages.getString("sunday");

	/** 月曜日 */
	public static final String STRING_MONDAY = Messages.getString("monday");

	/** 火曜日 */
	public static final String STRING_TUESDAY = Messages.getString("tuesday");

	/** 水曜日 */
	public static final String STRING_WEDNESDAY = Messages.getString("wednesday");

	/** 木曜日 */
	public static final String STRING_THURSDAY = Messages.getString("thursday");

	/** 金曜日 */
	public static final String STRING_FRIDAY = Messages.getString("friday");

	/** 土曜日 */
	public static final String STRING_SATURDAY = Messages.getString("saturday");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String enumToString(WeekNoEnum type) {
		if (WeekNoEnum.SUNDAY.equals(type)) {
			return STRING_SUNDAY;
		} else if (WeekNoEnum.MONDAY.equals(type)) {
			return STRING_MONDAY;
		} else if (WeekNoEnum.TUESDAY.equals(type)) {
			return STRING_TUESDAY;
		} else if (WeekNoEnum.WEDNESDAY.equals(type)) {
			return STRING_WEDNESDAY;
		} else if (WeekNoEnum.THURSDAY.equals(type)) {
			return STRING_THURSDAY;
		} else if (WeekNoEnum.FRIDAY.equals(type)) {
			return STRING_FRIDAY;
		} else if (WeekNoEnum.SATURDAY.equals(type)) {
			return STRING_SATURDAY;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static WeekNoEnum stringToEnum(String string) {
		if (string.equals(STRING_SUNDAY)) {
			return WeekNoEnum.valueOf("SUNDAY");
		} else if (string.equals(STRING_MONDAY)) {
			return WeekNoEnum.valueOf("MONDAY");
		} else if (string.equals(STRING_TUESDAY)) {
			return WeekNoEnum.valueOf("TUESDAY");
		} else if (string.equals(STRING_WEDNESDAY)) {
			return WeekNoEnum.valueOf("WEDNESDAY");
		} else if (string.equals(STRING_THURSDAY)) {
			return WeekNoEnum.valueOf("THURSDAY");
		} else if (string.equals(STRING_FRIDAY)) {
			return WeekNoEnum.valueOf("FRIDAY");
		} else if (string.equals(STRING_SATURDAY)) {
			return WeekNoEnum.valueOf("SATURDAY");
		}
		return null;
	}
	
}
