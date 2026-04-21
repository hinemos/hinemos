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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.TimezoneUtil;

/**
 * 日付処理を管理するクラス<BR>
 * 
 * @version 6.1.0
 * @since 2.0.0
 * 
 */
public class DateUtil {

	private static Log log = LogFactory.getLog(DateUtil.class);
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
				formatter.setTimeZone(TimezoneUtil.getTimeZone());
				Date date = null;
				try {
					log.debug("timeString = " + timeString);
					date = formatter.parse(timeString);
					log.debug("date.getTime afterZoneformat = " + date.getTime());
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
	
	/**
	 * 日時文字列 をHinemosクライアント形式(yyyy/MM/dd HH:mm:ss)からISO8601形式(yyyy-MM-dd HH:mm:ss)に変換する。
	 * 
	 * @param dateString 日時文字列(yyyy/MM/dd HH:mm:ss)
	 * @return dateString 日時文字列(yyyy-MM-dd HH:mm:ss)
	 * 
	 */
	public static String convDateFormatHinemos2Iso8601(String dateString) throws NullPointerException, ParseException {

		final SimpleDateFormat hinemosDfm = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		final SimpleDateFormat iso8601Dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		if (dateString == null || dateString.isEmpty()) {
			return null;
		}

		//既に iso8601形式の文字列なら そのまま利用する。
		try {
			iso8601Dfm.parse(dateString).getTime();
			return dateString;
		} catch (ParseException e) {
			//次の処理に行く
		}

		// hinemos形式の文字列(yyyy/MM/dd HH:mm:ss)をiso8601(yyyy-MM-dd HH:mm:ss)に変換
		try {
			long epoch = hinemosDfm.parse(dateString).getTime();
			return iso8601Dfm.format(epoch);
		} catch (NullPointerException e) {
			throw e;
		} catch (ParseException e) {
			throw e;
		}
	}
	
	/**
	 * 日時文字列 をISO8601形式(yyyy-MM-dd HH:mm:ss)からHinemosクライアント形式(yyyy/MM/dd HH:mm:ss)に変換する。
	 * 
	 * @param dateString 日時文字列(yyyy-MM-dd HH:mm:ss)
	 * @return dateString 日時文字列(yyyy/MM/dd HH:mm:ss)
	 * 
	 */
	public static String convDateFormatIso86012Hinemos(String dateString) throws NullPointerException, ParseException {
		if (dateString == null || dateString.isEmpty()) {
			return null;
		}

		final SimpleDateFormat hinemosDfm = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		final SimpleDateFormat iso8601Dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		//既に hinemos形式の文字列なら そのまま利用する。
		try {
			hinemosDfm.parse(dateString).getTime();
			return dateString;
		} catch (ParseException e) {
			//次の処理に行く
		}
		
		try {
			// iso8601(yyyy-MM-dd HH:mm:ss)をhinemos形式の文字列(yyyy/MM/dd HH:mm:ss)に変換
			long epoch = iso8601Dfm.parse(dateString).getTime();
			return hinemosDfm.format(epoch);
		} catch (NullPointerException e) {
			throw e;
		} catch (ParseException e) {
			throw e;
		}
	}

	/**
	 * 日付文字列("yyyy/m/d"等)に対して、ゼロパディング処理等を実施し、"yyyy/mm/dd" 形式に変換します。
	 * 月と日が一桁の場合はゼロ詰めします。
	 *
	 * @param dateStr
	 *            変換する日付文字列。"yyyy/m/d" または "yyyy/mm/dd" の形式を期待します。
	 * @return "yyyy/mm/dd" の 日付文字列。 
	 *          入力が {@code null} の場合は {@code null}を返し、
	 *          日付文字列の形式が期待と一致しない場合は元の文字列を返します。
	 */
	public static String convertDateZeroPadding(String dateStr) {
		if (dateStr == null) {
			return null;
		}

		final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})/(\\d{1,2})/(\\d{1,2})");
		// Matcherを生成してマッチングを行う
		Matcher matcher = DATE_PATTERN.matcher(dateStr);

		// 変換処理
		StringBuffer result = new StringBuffer();
		if (matcher.find()) {
			String year = matcher.group(1);
			String month = String.format("%02d", Integer.parseInt(matcher.group(2)));
			String day = String.format("%02d", Integer.parseInt(matcher.group(3)));
			matcher.appendReplacement(result, year + "/" + month + "/" + day);
			matcher.appendTail(result);
			return result.toString();
		} else {
			// 形式が一致しない場合は、元の文字列をそのまま返す
			return dateStr;
		}
	}
}
