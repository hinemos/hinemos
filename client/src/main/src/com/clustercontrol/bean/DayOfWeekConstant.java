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

package com.clustercontrol.bean;

import java.util.Calendar;

import com.clustercontrol.util.Messages;

/**
 * クライアントで用いる曜日の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class DayOfWeekConstant {
	/** 日曜日 */
	public static final int TYPE_SUNDAY = Calendar.SUNDAY;

	/** 月曜日 */
	public static final int TYPE_MONDAY = Calendar.MONDAY;

	/** 火曜日 */
	public static final int TYPE_TUESDAY = Calendar.TUESDAY;

	/** 水曜日 */
	public static final int TYPE_WEDNESDAY = Calendar.WEDNESDAY;

	/** 木曜日 */
	public static final int TYPE_THURSDAY = Calendar.THURSDAY;

	/** 金曜日 */
	public static final int TYPE_FRIDAY = Calendar.FRIDAY;

	/** 土曜日 */
	public static final int TYPE_SATURDAY = Calendar.SATURDAY;

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
	/*
	 * 曜日の略文字
	 */
	/** 日曜日 */
	public static final String STRING_SUN = Messages.getString("sunday");

	/** 月曜日 */
	public static final String STRING_MON = Messages.getString("monday");

	/** 火曜日 */
	public static final String STRING_TUE = Messages.getString("tuesday");

	/** 水曜日 */
	public static final String STRING_WED = Messages.getString("wednesday");

	/** 木曜日 */
	public static final String STRING_THU = Messages.getString("thursday");

	/** 金曜日 */
	public static final String STRING_FRI = Messages.getString("friday");

	/** 土曜日 */
	public static final String STRING_SAT = Messages.getString("saturday");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == TYPE_SUNDAY) {
			return STRING_SUNDAY;
		} else if (type == TYPE_MONDAY) {
			return STRING_MONDAY;
		} else if (type == TYPE_TUESDAY) {
			return STRING_TUESDAY;
		} else if (type == TYPE_WEDNESDAY) {
			return STRING_WEDNESDAY;
		} else if (type == TYPE_THURSDAY) {
			return STRING_THURSDAY;
		} else if (type == TYPE_FRIDAY) {
			return STRING_FRIDAY;
		} else if (type == TYPE_SATURDAY) {
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
	public static int stringToType(String string) {
		if (string.equals(STRING_SUNDAY)) {
			return TYPE_SUNDAY;
		} else if (string.equals(STRING_MONDAY)) {
			return TYPE_MONDAY;
		} else if (string.equals(STRING_TUESDAY)) {
			return TYPE_TUESDAY;
		} else if (string.equals(STRING_WEDNESDAY)) {
			return TYPE_WEDNESDAY;
		} else if (string.equals(STRING_THURSDAY)) {
			return TYPE_THURSDAY;
		} else if (string.equals(STRING_FRIDAY)) {
			return TYPE_FRIDAY;
		} else if (string.equals(STRING_SATURDAY)) {
			return TYPE_SATURDAY;
		}
		return -1;
	}


	/**
	 * 種別から文字列(略文字)に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToStrAbbreviation(int type) {
		if (type == TYPE_SUNDAY) {
			return STRING_SUN;
		} else if (type == TYPE_MONDAY) {
			return STRING_MON;
		} else if (type == TYPE_TUESDAY) {
			return STRING_TUE;
		} else if (type == TYPE_WEDNESDAY) {
			return STRING_WED;
		} else if (type == TYPE_THURSDAY) {
			return STRING_THU;
		} else if (type == TYPE_FRIDAY) {
			return STRING_FRI;
		} else if (type == TYPE_SATURDAY) {
			return STRING_SAT;
		}
		return "";
	}
	/**
	 * 文字列(略文字)から種別に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static int strAbbreviationToType(String string) {
		if (string.equals(STRING_SUN)) {
			return TYPE_SUNDAY;
		} else if (string.equals(STRING_MON)) {
			return TYPE_MONDAY;
		} else if (string.equals(STRING_TUE)) {
			return TYPE_TUESDAY;
		} else if (string.equals(STRING_WED)) {
			return TYPE_WEDNESDAY;
		} else if (string.equals(STRING_THU)) {
			return TYPE_THURSDAY;
		} else if (string.equals(STRING_FRI)) {
			return TYPE_FRIDAY;
		} else if (string.equals(STRING_SAT)) {
			return TYPE_SATURDAY;
		}
		return -1;
	}

}
