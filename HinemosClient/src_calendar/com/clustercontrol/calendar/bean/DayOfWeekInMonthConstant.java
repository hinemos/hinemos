/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.calendar.bean;

import com.clustercontrol.util.Messages;

/**
 * クライアントで用いる曜日の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class DayOfWeekInMonthConstant {
	/** 第１ 曜日 */
	public static final int TYPE_FIRST = 1;//Calendar.SUNDAY;

	/** 第2 曜日 */
	public static final int TYPE_SECOND = 2;

	/** 第3 曜日*/
	public static final int TYPE_THIRD = 3;

	/** 第4 曜日*/
	public static final int TYPE_FOURTH = 4;

	/** 第5 曜日*/
	public static final int TYPE_FIFTH = 5;

	/** 毎週*/
	public static final int TYPE_EVERYWEEK = 0;

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
	public static String typeToString(int type) {
		if (type == TYPE_FIRST) {
			return STRING_FIRST;
		} else if (type == TYPE_SECOND) {
			return STRING_SECOND;
		} else if (type == TYPE_THIRD) {
			return STRING_THIRD;
		} else if (type == TYPE_FOURTH) {
			return STRING_FOURTH;
		} else if (type == TYPE_FIFTH) {
			return STRING_FIFTH;
		} else if (type == TYPE_EVERYWEEK) {
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
	public static int stringToType(String string) {
		if (string.equals(STRING_FIRST)) {
			return TYPE_FIRST;
		} else if (string.equals(STRING_SECOND)) {
			return TYPE_SECOND;
		} else if (string.equals(STRING_THIRD)) {
			return TYPE_THIRD;
		} else if (string.equals(STRING_FOURTH)) {
			return TYPE_FOURTH;
		} else if (string.equals(STRING_FIFTH)) {
			return TYPE_FIFTH;
		} else if (string.equals(STRING_EVERYWEEｋ)) {
			return TYPE_EVERYWEEK;
		}
		return -1;
	}
}
