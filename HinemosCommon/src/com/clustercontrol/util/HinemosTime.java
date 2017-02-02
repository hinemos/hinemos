/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HinemosManager内で独自に管理する現在時刻（Hinemos時刻）を保持するクラス<br/>
 * <br>
 * TODO Ver5.1時点ではスケジューラ毎に異なるHinemos時刻をもてないため、
 * どのスケジューラもstaticなメソッドと同じ時刻を返すが、将来的にはスケジューラごとに異なる時刻を返せるようにする
 * 
 */

public class HinemosTime {

	private static Log log = LogFactory.getLog(HinemosTime.class);

	private static volatile long timeOffsetMillis = 0;
	private static volatile TimeZone timeZone = TimeZone.getDefault();
	
	
	/**
	 * HinemosManagerで管理する現在時刻（Hinemos時刻）を返す。<br>
	 * @return
	 */
	public static long currentTimeMillis(){
		return System.currentTimeMillis() + timeOffsetMillis;
	}

	/**
	 * Hinemos時刻ベースのDateクラスのインスタンスを返す。<br>
	 * @return
	 */
	public static Date getDateInstance() {
		return new Date(HinemosTime.currentTimeMillis());
	}
	
	/**
	 * Hinemos時刻及びHinemosManagerで管理するタイムゾーンベースの日付文字列を返す。<br>
	 * @return
	 */
	public static String getDateString() {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss XXX yyyy", Locale.US);
		sdf.setTimeZone(HinemosTime.getTimeZone());
		return sdf.format(currentTimeMillis());
	}
	
	/**
	 * Hinemos時刻及びHinemosManagerで管理するタイムゾーンベースのCalendarクラスのインスタンスを返す。<br>
	 * @return
	 */
	public static Calendar getCalendarInstance() {
		Calendar calendar = Calendar.getInstance(getTimeZone());
		calendar.setTimeInMillis(HinemosTime.currentTimeMillis());
		return calendar;
	}
	
	/**
	 * Hinemos時刻ベース及び指定されたタイムゾーンベースのCalendarクラスのインスタンスを返す。<br>
	 * @param timezone タイムゾーン
	 * @return
	 */
	public static Calendar getCalendarInstance(TimeZone timezone) {
		Calendar calendar = Calendar.getInstance(timezone);
		calendar.setTimeInMillis(calendar.getTimeInMillis() + timeOffsetMillis);
		return calendar;
	}
	
	/**
	 * Hinemos時刻及びHinemosManagerで管理するタイムゾーンベースのCalendarクラスのインスタンスを返す。<br>
	 * @param locale ロケール
	 * @return
	 */
	public static Calendar getCalendarInstance(Locale locale) {
		Calendar calendar = Calendar.getInstance(getTimeZone(), locale);
		calendar.setTimeInMillis(calendar.getTimeInMillis() + timeOffsetMillis);
		return calendar;
	}
	
	/**
	 * Hinemos時刻ベース及び指定されたタイムゾーンベースのCalendarクラスのインスタンスを返す。<br>
	 * @param timezone タイムゾーン
	 * @param locale ロケール
	 * @return
	 */
	public static Calendar getCalendarInstance(TimeZone timezone, Locale locale) {
		Calendar calendar = Calendar.getInstance(timezone, locale);
		calendar.setTimeInMillis(calendar.getTimeInMillis() + timeOffsetMillis);
		return calendar;
	}
	
	/**
	 * システム時間に対するスケジューラ時間のオフセット（ミリ秒）を取得する。
	 * @return timeOffsetMillis システム時間に対するスケジューラ時間のオフセット（ミリ秒）
	 */
	public static long getTimeOffsetMillis() {
		log.debug("getTimeOffsetMillis():" + timeOffsetMillis);
		return timeOffsetMillis;
	}
	
	/**
	 * システム時間に対するスケジューラ時間のオフセット（ミリ秒）を設定する。
	 * @param offset システム時間に対するスケジューラ時間のオフセット（ミリ秒）
	 */
	public static void setTimeOffsetMillis(long offset) {
		log.debug("setTimeOffsetMillis(): " + timeOffsetMillis + " -> " + offset);
		timeOffsetMillis = offset;
	}

	/**
	 * HinemosManagerで管理するタイムゾーンを返す。<br>
	 * @return timeZone タイムゾーン
	 */
	public static TimeZone getTimeZone() {
		log.debug("getTimeZone():" + timeZone);
		return timeZone;
	}
	
	/**
	 * HinemosManagerで管理するタイムゾーンにおけるUTCからのオフセットを返す。<br>
	 * @return timeZone タイムゾーンオフセット
	 */
	public static int getTimeZoneOffset() {
		log.debug("getTimezoneOffset():" + timeZone.getRawOffset());
		return timeZone.getRawOffset();
	}
	
	/**
	 * HinemosManagerで管理するタイムゾーンをUTCからのオフセットで設定する。
	 * @param offset
	 */
	public static void setTimeZoneOffset(int offset) {
		log.debug("setTimeZoneOffset(): " + getTimeZoneOffset() + " -> " + offset);
		timeZone.setRawOffset(offset);
	}

}
