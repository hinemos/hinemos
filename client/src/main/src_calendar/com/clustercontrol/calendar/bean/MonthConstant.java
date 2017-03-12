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

import java.util.Calendar;
import com.clustercontrol.util.Messages;

/**
 * クライアントで用いる月の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class MonthConstant {

	/**
	 * java.util.Calendarクラスでは、
	 * JANUARY = 0 ..... DECEMBER = 11
	 * そのため、+1 によって、数値と月ナンバーをそろえる
	 */

	/** 毎月 */
	public static final int TYPE_ALLOFMONTH = 0;

	/** １月 */
	public static final int TYPE_JANUARY = Calendar.JANUARY + 1;

	/** 2月 */
	public static final int TYPE_FEBRUARY = Calendar.FEBRUARY + 1;

	/** 3月 */
	public static final int TYPE_MARCH = Calendar.MARCH + 1;

	/** 4月 */
	public static final int TYPE_APRIL = Calendar.APRIL + 1;

	/** 5月 */
	public static final int TYPE_MAY = Calendar.MAY + 1;

	/** 6月 */
	public static final int TYPE_JUNE = Calendar.JUNE + 1;

	/** 7月 */
	public static final int TYPE_JULY = Calendar.JULY + 1;

	/** 8月 */
	public static final int TYPE_AUGUST = Calendar.AUGUST + 1;

	/** 9月 */
	public static final int TYPE_SEPTEMBER = Calendar.SEPTEMBER + 1;

	/** 10月 */
	public static final int TYPE_OCTOBER = Calendar.OCTOBER + 1;

	/** 11月 */
	public static final int TYPE_NOVEMBER = Calendar.NOVEMBER + 1;

	/** 12月 */
	public static final int TYPE_DECEMBER = Calendar.DECEMBER + 1;

	/** 毎月 */
	public static final String STRING_ALLOFMONTH = Messages.getString("all.month");

	/** １月 */
	public static final String STRING_JANUARY = Messages.getString("month.january");

	/** 2月 */
	public static final String STRING_FEBRUARY = Messages.getString("month.february");

	/** 3月 */
	public static final String STRING_MARCH = Messages.getString("month.march");

	/** 4月 */
	public static final String STRING_APRIL = Messages.getString("month.april");

	/** 5月 */
	public static final String STRING_MAY = Messages.getString("month.may");

	/** 6月 */
	public static final String STRING_JUNE = Messages.getString("month.june");

	/** 7月 */
	public static final String STRING_JULY = Messages.getString("month.july");

	/** 8月 */
	public static final String STRING_AUGUST = Messages.getString("month.august");

	/** 9月 */
	public static final String STRING_SEPTEMBER = Messages.getString("month.september");

	/** 10月 */
	public static final String STRING_OCTOBER = Messages.getString("month.october");

	/** 11月 */
	public static final String STRING_NOVEMBER = Messages.getString("month.november");

	/** 12月 */
	public static final String STRING_DECEMBER = Messages.getString("month.december");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if(type == TYPE_ALLOFMONTH){
			return STRING_ALLOFMONTH;

		} else if (type == TYPE_JANUARY) {
			return STRING_JANUARY;

		} else if (type == TYPE_FEBRUARY) {
			return STRING_FEBRUARY;

		} else if (type == TYPE_MARCH) {
			return STRING_MARCH;

		} else if (type == TYPE_APRIL) {
			return STRING_APRIL;

		} else if (type == TYPE_MAY) {
			return STRING_MAY;

		} else if (type == TYPE_JUNE) {
			return STRING_JUNE;

		} else if (type == TYPE_JULY) {
			return STRING_JULY;

		} else if (type == TYPE_AUGUST) {
			return STRING_AUGUST;

		} else if (type == TYPE_SEPTEMBER) {
			return STRING_SEPTEMBER;

		} else if (type == TYPE_OCTOBER) {
			return STRING_OCTOBER;

		} else if (type == TYPE_NOVEMBER) {
			return STRING_NOVEMBER;

		} else if (type == TYPE_DECEMBER) {
			return STRING_DECEMBER;
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


		if(string.equals(STRING_ALLOFMONTH)){
			return TYPE_ALLOFMONTH;
		} else if (string.equals(STRING_JANUARY)) {
			return TYPE_JANUARY;
		} else if (string.equals(STRING_FEBRUARY)) {
			return TYPE_FEBRUARY;
		} else if (string.equals(STRING_MARCH)) {
			return TYPE_MARCH;
		} else if (string.equals(STRING_APRIL)) {
			return TYPE_APRIL;
		} else if (string.equals(STRING_MAY)) {
			return TYPE_MAY;
		} else if (string.equals(STRING_JUNE)) {
			return TYPE_JUNE;
		} else if (string.equals(STRING_JULY)) {
			return TYPE_JULY;
		} else if (string.equals(STRING_AUGUST)) {
			return TYPE_AUGUST;
		} else if (string.equals(STRING_SEPTEMBER)) {
			return TYPE_SEPTEMBER;
		} else if (string.equals(STRING_OCTOBER)) {
			return TYPE_OCTOBER;
		} else if (string.equals(STRING_NOVEMBER)) {
			return TYPE_NOVEMBER;
		} else if (string.equals(STRING_DECEMBER)) {
			return TYPE_DECEMBER;
		}
		return -1;
	}
}
