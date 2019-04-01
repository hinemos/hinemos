/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

/**
 * 日付を扱うためのUtil<br>
 * <br>
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
package com.clustercontrol.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.InvalidSetting;

public class DateUtil {

	// ログ出力関連.
	/** ロガー */
	private static Log log = LogFactory.getLog(DateUtil.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * ミリ秒を読みやすい桁の単位に変換.<br>
	 * <br>
	 * 端数は切捨て.
	 * 
	 * @param originTime
	 *            変換対象の時間(ミリ秒)
	 * @return 変換後のミリ秒、日付
	 * @throws InvalidSetting
	 * 
	 */
	public static TimeUnitSet getHumanicTime(long originTime) {
		TimeUnitSet returnTime = new TimeUnitSet();

		long seconds = TimeUnit.MILLISECONDS.toSeconds(originTime);
		if (seconds == 0) {
			returnTime.setUnit("milli.sec");
			returnTime.setTime(originTime);
			return returnTime;
		}

		long minutes = TimeUnit.MILLISECONDS.toMinutes(originTime);
		if (minutes == 0) {
			returnTime.setUnit("second");
			returnTime.setTime(seconds);
			return returnTime;
		}

		long hours = TimeUnit.MILLISECONDS.toHours(originTime);
		if (hours == 0) {
			returnTime.setUnit("minute");
			returnTime.setTime(minutes);
			return returnTime;
		}

		long days = TimeUnit.MILLISECONDS.toDays(originTime);
		if (days == 0) {
			returnTime.setUnit("hour.period");
			returnTime.setTime(hours);
			return returnTime;
		}

		returnTime.setUnit("day");
		returnTime.setTime(days);
		return returnTime;

	}

	/**
	 * 時間と単位のセット.
	 */
	public static class TimeUnitSet {

		/** 時間 */
		private long time = 0L;
		/**
		 * 単位<br>
		 * messages_client.propertiesに対応した文字列.
		 */
		private String unit = null;

		/** 時間 */
		public long getTime() {
			return time;
		}

		/** 時間 */
		public void setTime(long time) {
			this.time = time;
		}

		/**
		 * 単位<br>
		 * messages_client.propertiesに対応した文字列.
		 */
		public String getUnit() {
			return unit;
		}

		/**
		 * 単位<br>
		 * messages_client.propertiesに対応した文字列.
		 */
		public void setUnit(String unit) {
			this.unit = unit;
		}
	}

	/**
	 * 日付指定の文字列をDB検索用にLong値変換.<br>
	 * 
	 * @param dateStr
	 *            変換元日付文字列(指定なしは現在時刻を取得)
	 * @param format
	 *            日付フォーマット(SimpleDateFormat)
	 * @return 変換後のミリ秒、日付
	 * @throws InvalidSetting
	 * 
	 */
	public static Long dateStrToMillis(String dateStr, String format) throws InvalidSetting {
		return DateUtil.dateStrToMillis(dateStr, format, true);
	}

	/**
	 * 日付指定の文字列をDB検索用にLong値変換.<br>
	 * 
	 * @param dateStr
	 *            変換元日付文字列(指定なしは現在時刻を取得)
	 * @param format
	 *            日付フォーマット(SimpleDateFormat)
	 * @param reverseCheck
	 *            変換後、Long値→日付文字列で逆算して桁数正しいかのチェックを行うかどうか.
	 * @return 変換後のミリ秒、日付
	 * @throws InvalidSetting
	 * 
	 */
	public static Long dateStrToMillis(String dateStr, String format, boolean reverseCheck) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		Long dateMillis = null;
		// 日付指定がない場合は現在時刻を設定する.
		if (dateStr == null || dateStr.isEmpty()) {
			dateMillis = Long.valueOf(HinemosTime.currentTimeMillis());
			return dateMillis;
		}

		// 引数の日付がString→DATE型に変換可能か(不正な日付文字列が渡されていないか).
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		dateFormat.setLenient(false);
		Date date = null;
		try {
			date = dateFormat.parse(dateStr);
			dateMillis = date.getTime();
		} catch (ParseException e) {
			// 引数不正.
			message = String.format("failed to parse date. date(origin)=[%s]", dateStr);
			log.info(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// ループ防止.
		if (!reverseCheck) {
			return dateMillis;
		}

		// 引数の桁数がフォーマットと違っても変換されるのでチェック.
		String reverse = DateUtil.millisToString(dateMillis, format);
		if (!dateStr.equals(reverse)) {
			message = String.format("invalid date string. date(origin)=[%s], format=[%s]", dateStr, format);
			log.info(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		return dateMillis;
	}

	/**
	 * UNIXタイムを日付文字列に変換.<br>
	 * <br>
	 * DATE型を利用した変換だとException発生しないで落ちるケースがあるのでutil化.
	 * 
	 * @param millis
	 *            UNIXタイム(ミリ秒)
	 * @param format
	 *            日付フォーマット(SimpleDateFormatの指定)
	 * @return 指定の日付文字列、変換不可はnull返却
	 * @throws InvalidSetting
	 *             フォーマット不正.
	 */
	public static String millisToString(Long millis, String format) throws InvalidSetting {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String message = null;

		// 時間nullはありえるのでExceptionしない.
		if (millis == null) {
			return null;
		}

		// フォーマット不正は不具合の可能性あるのでException.
		if (format == null || format.isEmpty()) {
			message = "the necessary argument 'format' is empty.";
			log.warn(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}
		SimpleDateFormat dateFormat;
		try {
			dateFormat = new SimpleDateFormat(format);
			dateFormat.setLenient(false);
		} catch (IllegalArgumentException e) {
			message = String.format("the argument 'format' is invalid. format=[%s]", format);
			log.info(methodName + DELIMITER + message);
			throw new InvalidSetting(message);
		}

		// ここが引数不正の場合にException発生しない.
		Date date = new Date(millis.longValue());
		String dateStr = dateFormat.format(date);

		// 指定フォーマットに変換できてない場合はnull.
		if (dateStr == null || dateStr.isEmpty()) {
			return null;
		}

		return dateStr;
	}

}
