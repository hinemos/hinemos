/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 
 * Long型から日付形式の文字列に変換するクラス
 *
 */
public class DateTimeStringConverter {
	/**
	 * Long型の時間をString型に変換する。
	 */
	public static String formatLongDate(Long dateValue){
		String dateString = "";
		if (dateValue == null) {
			return dateString;
		}
		
		SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
		
		try { 
			dateString = sdf.format(new Date(dateValue));
		} catch (Exception e) {
			//ignore
		}
		return dateString;
	}

	/**
	 * Long型の時間をString型に変換する。
	 */
	public static String formatLongDate(Long dateValue, String formatString){
		String dateString = "";
		if (dateValue == null) {
			return dateString;
		}
		
		SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat(formatString);
		
		try { 
			dateString = sdf.format(new Date(dateValue));
		} catch (Exception e) {
			//ignore
		}
		return dateString;
	}
	
	/**
	 * Date型の時間をString型に変換する。
	 */
	public static String formatDate(Date date) {
		String dateString = "";
		if (date == null) {
			return dateString;
		}
		try {
			dateString = TimezoneUtil.getSimpleDateFormat().format(date);
		} catch (Exception e) {
			//ignore
		}
		return dateString;
	}

	/**
	 * Date型の時間をString型に変換する。
	 */
	public static String formatDate(Date date, String formatString) {
		String dateString = "";
		if (date == null) {
			return dateString;
		}
		try {
			dateString = TimezoneUtil.getSimpleDateFormat(formatString).format(date);
		} catch (Exception e) {
			//ignore
		}
		return dateString;
	}

	
	/**
	 * String型の時間をDate型に変換する。
	 */
	public static Date parseDateString(String dateString) {
		Date date = null;
		try {
			date = TimezoneUtil.getSimpleDateFormat().parse(dateString);
		} catch (Exception e) {
			//ignore
		}
		return date;
	}

	/**
	 * String型の時間をDate型に変換する。
	 */
	public static Date parseDateString(String dateString, String formatString) {
		Date date = null;
		try {
			date = TimezoneUtil.getSimpleDateFormat(formatString).parse(dateString);
		} catch (Exception e) {
			//ignore
		}
		return date;
	}

	/**
	 * String型の時間をオフセットの計算なしでLong型に変換する。<BR>
	 * 
	 * マネージャからすでにオフセットの計算済みの日時が返ってくる場合など、<BR>
	 * クライアントではそのままの値で変換したい時に利用してください。<BR>
	 */
	public static Long convertDateStringWithoutOffset(String dateString, String formatString) {
		Long dateValue = 0L;
		try {
			SimpleDateFormat format = new SimpleDateFormat(formatString);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			dateValue = format.parse(dateString).getTime();
		} catch (Exception e) {
			// ignore
		}
		return dateValue;
	}

	/**
	 * Long型の時間をオフセットの計算なしでString型に変換する。<BR>
	 * 
	 * マネージャからすでにオフセットの計算済みの日時が返ってくる場合など、<BR>
	 * クライアントではそのままの値で変換したい時に利用してください。<BR>
	 */
	public static String formatLongDateWithoutOffset(Long dateValue, String formatString) {
		String dateString = "";
		if (dateValue == null) {
			return dateString;
		}
		try {
			SimpleDateFormat format = new SimpleDateFormat(formatString);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			dateString = format.format(new Date(dateValue));
		} catch (Exception e) {
			// ignore
		}
		return dateString;
	}

}
