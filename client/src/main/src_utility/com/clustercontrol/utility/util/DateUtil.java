/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日付処理を管理するクラス<BR>
 * 
 * @version 6.1.0
 * @since 2.0.0
 * 
 */
public class DateUtil {

	/**
	 * コンストラクタ
	 * 
	 * @version 2.0.0
	 * @since 2.0.0
	 */
	private DateUtil() {

	}

	/**
	 * UNIX Epoch秒を日時文字列(yyyy/MM/dd HH:mm:ss)に変換する。
	 * 
	 * @param epoch Epoch秒
	 * @return 日時文字列(yyyy/MM/dd HH:mm:ss)
	 * 
	 * @version 2.0.0
	 * @since 2.0.0
	 */
	public static String convEpoch2DateString(long epoch) throws NullPointerException, IllegalArgumentException {

		String dateString = null;

		try {
			// Epoch秒を日時文字列(yyyy/MM/dd HH:mm:ss)に変換
			DateFormat dfm = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			dateString = dfm.format(new Date(epoch));
		} catch (NullPointerException e) {
				throw e;

		} catch (IllegalArgumentException e) {
				throw e;
		}

		return dateString;
	}

	/**
	 * UNIX Epoch秒を時刻文字列(HH:mm:ss)に変換する。
	 * 
	 * @param epoch Epoch秒
	 * @return 時刻字列(HH:mm:ss)
	 * 
	 * @version 2.2.0
	 * @since 2.0.0
	 */
	public static String convEpoch2TimeString(long epoch) {

		String timeString = null;

		// Epoch秒を時刻文字列(HH:mm:ss)に変換
		timeString = TimeTo48hConverter.dateTo48hms(epoch);

		return timeString;
	}

	/**
	 * 日時文字列(yyyy/MM/dd HH:mm:ss)をUNIX Epoch秒に変換する。
	 * 
	 * @param dateString 日時文字列(yyyy/MM/dd HH:mm:ss)
	 * @return Epoch秒
	 * 
	 * @version 2.0.0
	 * @since 2.0.0
	 */
	public static long convDateString2Epoch(String dateString) throws NullPointerException, ParseException {

		long epoch = -1;

		try {
			if (dateString != null && !dateString.isEmpty()) {
				// 日時形式の文字列(yyyy/MM/dd HH:mm:ss)をEpoch秒に変換
				DateFormat dfm = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				epoch = dfm.parse(dateString).getTime();
			}
		} catch (NullPointerException e) {
			throw e;

		} catch (ParseException e) {
			throw e;

		}

		return epoch;
	}

	/**
	 * 時刻文字列(HH:mm:ss)をUNIX Epoch秒に変換する。
	 * 
	 * @param timeString 時刻文字列(HH:mm:ss)
	 * @return Epoch秒
	 * 
	 * @version 2.2.0
	 * @since 2.0.0
	 */
	public static long convTimeString2Epoch(String timeString) throws ParseException {

		long epoch = -1;

		try {
			if (timeString != null && !timeString.isEmpty()) {
				// 時刻形式の文字列(HH:mm:ss)をEpoch秒に変換
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
				Date date = null;
				try {
					date = formatter.parse(timeString);
					epoch = date.getTime();
				} catch (ParseException e) {
					formatter = new SimpleDateFormat("HH:mm");
					date = formatter.parse(timeString);
					epoch = date.getTime();
				}
			}
		} catch (ParseException e) {
			throw e;
		}

		return epoch;
	}
}
