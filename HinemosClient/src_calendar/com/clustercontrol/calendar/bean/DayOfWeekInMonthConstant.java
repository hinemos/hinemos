/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.bean;

import org.openapitools.client.model.CalendarDetailInfoResponse.WeekXthEnum;

import com.clustercontrol.util.Messages;

/**
 * クライアントで用いる曜日の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class DayOfWeekInMonthConstant {

	/** 第１ 曜日 */
	public static final String STRING_FIRST = Messages.getString("calendar.detail.first");

	/** 第2 曜日 */
	public static final String STRING_SECOND= Messages.getString("calendar.detail.second");

	/** 第3 曜日*/
	public static final String STRING_THIRD = Messages.getString("calendar.detail.third");

	/** 第4 曜日*/
	public static final String STRING_FOURTH = Messages.getString("calendar.detail.fourth");

	/** 第5 曜日*/
	public static final String STRING_FIFTH = Messages.getString("calendar.detail.fifth");

	/** 毎週*/
	public static final String STRING_EVERYWEEｋ = Messages.getString("calendar.detail.everyweek");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String enumToString(WeekXthEnum type) {
		if (WeekXthEnum.FIRST_WEEK.equals(type)) {
			return STRING_FIRST;
		} else if (WeekXthEnum.SECOND_WEEK.equals(type)) {
			return STRING_SECOND;
		} else if (WeekXthEnum.THIRD_WEEK.equals(type)) {
			return STRING_THIRD;
		} else if (WeekXthEnum.FOURTH_WEEK.equals(type)) {
			return STRING_FOURTH;
		} else if (WeekXthEnum.FIFTH_WEEK.equals(type)) {
			return STRING_FIFTH;
		} else if (WeekXthEnum.EVERY_WEEK.equals(type)) {
			return STRING_EVERYWEEｋ;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static WeekXthEnum stringToEnum(String string) {
		if (string.equals(STRING_FIRST)) {
			return WeekXthEnum.FIRST_WEEK;
		} else if (string.equals(STRING_SECOND)) {
			return WeekXthEnum.SECOND_WEEK;
		} else if (string.equals(STRING_THIRD)) {
			return WeekXthEnum.THIRD_WEEK;
		} else if (string.equals(STRING_FOURTH)) {
			return WeekXthEnum.FOURTH_WEEK;
		} else if (string.equals(STRING_FIFTH)) {
			return WeekXthEnum.FIFTH_WEEK;
		} else if (string.equals(STRING_EVERYWEEｋ)) {
			return WeekXthEnum.EVERY_WEEK;
		}
		return null;
	}
}
