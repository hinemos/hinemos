/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.systemlog.bean;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.HinemosTime;

/**
 * syslog情報を保持するクラス
 */
public class SyslogMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// システムログ日付の有効範囲(デフォルト値)
	public static final int SYSLOG_DEFAULT_PERIOD_HOUR = 24;
	// システムログフォーマットのインデックス：プライオリティ
	private static final int SYSLOG_FORMAT_PRIORITY = 0;
	// システムログフォーマットのインデックス：月
	private static final int SYSLOG_FORMAT_MONTH = 1;
	// システムログフォーマットのインデックス：日
	private static final int SYSLOG_FORMAT_DAY = 2;
	// システムログフォーマットのインデックス：時
	private static final int SYSLOG_FORMAT_HH = 3;
	// システムログフォーマットのインデックス：分
	private static final int SYSLOG_FORMAT_MM = 4;
	// システムログフォーマットのインデックス：秒
	private static final int SYSLOG_FORMAT_SS = 5;
	// システムログフォーマットのインデックス：ホスト
	private static final int SYSLOG_FORMAT_HOSTNAME = 6;
	// システムログフォーマットのインデックス：メッセージ
	private static final int SYSLOG_FORMAT_MESSAGE = 7;
	// システムログをチェックする年数
	private static final int LEAPYEAR_CHECK_COUNT = 7;
	
	
	private static Log log = LogFactory.getLog(SyslogMessage.class);

	public Facility facility;
	public Severity severity;
	public long date;
	public String hostname;
	public String message;
	public String rawSyslog;

	public enum Facility { KERN, USER, MAIL, DAEMON, AUTH, SYSLOG, LPR, NEWS, UUCP, CRON, AUTHPRIV, FTP, NTP, SECURITY, CONSOLE,
		LOCAL0, LOCAL1, LOCAL2, LOCAL3, LOCAL4, LOCAL5, LOCAL6, LOCAL7 };

		public enum Severity { EMERG, ALERT, CRIT, ERR, WARNING, NOTICE, INFO, DEBUG };

		public SyslogMessage() {

		}

		public SyslogMessage(Facility facility, Severity severity, long date, String hostname, String message, String rawSyslog) {
			this.facility = facility;
			this.severity = severity;
			this.date = date;
			this.hostname = hostname;
			this.message = message;
			this.rawSyslog = rawSyslog;
		}

		/**
		 * syslogの文字列をパースし、その解析結果をSyslogMessageインスタンスとして返すAPI<br>
		 * <br>
		 * <b>システムログの年算出方法の概要</b><br>
		 * 1. システム日付の年の前年、当年、翌年とシステムログ日付の月日時分ミリ秒を組み合わせた３パターンの年月日を用意する<br>
		 * 2. 現在日付と３パターンの時間差を算出し最も時間差の小さい年をシステムログの年とする<br>
		 * @param syslog syslogの文字列
		 * @return SyslogMessageインスタンス
		 * @throws ParseException syslogのフォーマットに従っていない文字列が与えられた場合
		 * @throws HinemosUnknown 
		 */
		public static SyslogMessage parse(String syslog) throws ParseException, HinemosUnknown {
			if (log.isDebugEnabled()) {
				log.debug("parsing syslog : " + syslog);
			}

			// [0]:プライオリティ, [1]:月(MMM), [2]:日(dd), [3]:時(HH), [4]:分(mm), [5]:秒(ss), [6]:ホスト名, [7]:メッセージ
			// システムログの時間フォーマットを{1,date,MMM dd HH:mm:ss}で取得した場合、年指定がないため、1970年として扱われる。
			// その結果、システムログ日付が閏年で2/29の場合、3/1に置き換えられる。(1970年は閏年でないため)
			MessageFormat syslogFormat = new MessageFormat("<{0,number,integer}>{1} {2} {3}:{4}:{5} {6} {7}", Locale.ENGLISH);

			// 文字列をパースしてヘッダ情報およびメッセージを取得する
			Object[] syslogArgs = null;
			try {
				syslogArgs = syslogFormat.parse(syslog);
			} catch (ParseException e) {
				log.info("ParseException1 : message=" + e.getMessage() + ", syslog=" + syslog);
				throw e;
			} 
			
			if ("".equals(syslogArgs[SYSLOG_FORMAT_DAY])) {
				try {
				// RFC3164を準拠し、月と日の間にスペースが付加され日が取得できない場合、再フォーマットを実施する
					syslogFormat = new MessageFormat("<{0,number,integer}>{1}  {2} {3}:{4}:{5} {6} {7}", Locale.ENGLISH);
					syslogArgs = syslogFormat.parse(syslog);
				} catch (ParseException e) {
					log.info("ParseException2 : message=" + e.getMessage() + ", syslog=" + syslog);
					throw e;
				}
			}

			if (syslogArgs == null)
				throw new HinemosUnknown("different syslog pattern");
				
			if (log.isDebugEnabled()) {
				int i = 0;
				for (Object arg : syslogArgs) {
					log.debug(String.format("syslog args [%d] : %s", i++, arg.toString()));
				}
			}

			// システムログ時刻が現在時刻より未来の場合に許容する時間(H)
			Integer syslogEffectiveTime = HinemosPropertyCommon.monitor_systemlog_period_hour.getIntegerValue();

			// 0以下が設定された場合は、デフォルト値に置き換え
			if (syslogEffectiveTime <= 0) {
				syslogEffectiveTime = SYSLOG_DEFAULT_PERIOD_HOUR;
			}

			Calendar nowCal = HinemosTime.getCalendarInstance();

			// 翌年でシステムログ年月日を生成(年跨ぎを考慮)
			int year = nowCal.get(Calendar.YEAR) + 1;
			int month = editCalendarMonth((String) syslogArgs[SYSLOG_FORMAT_MONTH]);
			int dayOfMonth = Integer.parseInt((String) syslogArgs[SYSLOG_FORMAT_DAY]);
			int hourOfDay = Integer.parseInt((String) syslogArgs[SYSLOG_FORMAT_HH]);
			int minute = Integer.parseInt((String) syslogArgs[SYSLOG_FORMAT_MM]);
			int second = Integer.parseInt((String) syslogArgs[SYSLOG_FORMAT_SS]);

			// システムログの候補日
			List<Calendar> checkCalList = new ArrayList<Calendar>();
			// システムログ時刻(現在時刻＋許容する時間）
			Calendar syslogEffectiveCal = (Calendar) nowCal.clone();
			syslogEffectiveCal.add(Calendar.HOUR, syslogEffectiveTime);

			// システムログ年の候補日を選出する
			for (int i = 0; checkCalList.size() < 2; i++) {

				// 最大繰り返し回数以上は処理しない
				if (LEAPYEAR_CHECK_COUNT <= i) {
					break;
				}
				Calendar syslogCheckCal = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
				year--;

				// 許容する時間の範囲外は候補日としない(有効時刻＜編集後シスログ時刻の場合)
				if (syslogEffectiveCal.compareTo(syslogCheckCal) < 0) {
					continue;
				}
				// 検査対象年が閏日でシステムログの日と一致しない場合は候補日としない
				if (dayOfMonth != syslogCheckCal.get(Calendar.DAY_OF_MONTH)) {
					continue;
				}
				// 候補日とする場合に追加
				checkCalList.add(syslogCheckCal);
			}

			Calendar editSyslogCal = null;

			// 現在時刻から直近の候補日を選定する
			if (checkCalList.size() > 0) {
				long absMinMillis = Long.MAX_VALUE;
				for (int i = 0; i < checkCalList.size(); i++) {
					long absDiff = Math.abs(checkCalList.get(i).getTimeInMillis() - nowCal.getTimeInMillis());
					if (absDiff < absMinMillis) {
						// 候補日で現在時刻から最小の時間差の日付
						absMinMillis = absDiff;
						editSyslogCal = checkCalList.get(i);
					}
				}
			} else {
				// 不正なシステムログ日付は、現在時刻の年
				editSyslogCal = new GregorianCalendar(nowCal.get(Calendar.YEAR), month, dayOfMonth, hourOfDay, minute, second);
				log.warn("System log date is invalid : " + syslog);
			}

			int pri = ((Long) syslogArgs[SYSLOG_FORMAT_PRIORITY]).intValue();
			String hostname = (String) syslogArgs[SYSLOG_FORMAT_HOSTNAME];
			String msg = (String) syslogArgs[SYSLOG_FORMAT_MESSAGE];
			Date date = editSyslogCal.getTime();

			// インスタンスの生成
			SyslogMessage instance = new SyslogMessage(getFacility((int) pri), getSeverity((int) pri), date.getTime(), hostname, msg, syslog);
			if (log.isDebugEnabled()) {
				log.debug("parsed syslog : " + instance);
			}

			return instance;
		}

		/**
		 * 月の型変換
		 * システムログの月(String)をCalendarクラスの月(int)に変換する
		 * 
		 * @param month 月
		 * @return Calendarクラスの月
		 */
		private static int editCalendarMonth(String month) {

			switch (month.toUpperCase()) {
			case "JAN":
				return Calendar.JANUARY;
			case "FEB":
				return Calendar.FEBRUARY;
			case "MAR":
				return Calendar.MARCH;
			case "APR":
				return Calendar.APRIL;
			case "MAY":
				return Calendar.MAY;
			case "JUN":
				return Calendar.JUNE;
			case "JUL":
				return Calendar.JULY;
			case "AUG":
				return Calendar.AUGUST;
			case "SEP":
				return Calendar.SEPTEMBER;
			case "OCT":
				return Calendar.OCTOBER;
			case "NOV":
				return Calendar.NOVEMBER;
			case "DEC":
				return Calendar.DECEMBER;
			}
			return Calendar.UNDECIMBER;
		}

		@Override
		public String toString() {
			return String.format("%s [facility = %s, severity = %s, date = %s, hostname = %s, message = %s]",
					this.getClass().getSimpleName(), facility, severity, new Date(date), hostname, message);
		}

		/**
		 * syslogヘッダの<PRI>から該当するFacility値を返すAPI
		 * @param pri syslogヘッダの<PRI>部の値
		 * @return Facility値
		 */
		public static Facility getFacility(int pri) {
			int facility = (pri & 0xFFFFFFF8) >> 3;

			switch (facility) {
			case 0 :
				return Facility.KERN;
			case 1 :
				return Facility.USER;
			case 2 :
				return Facility.MAIL;
			case 3 :
				return Facility.DAEMON;
			case 4 :
				return Facility.AUTH;
			case 5 :
				return Facility.SYSLOG;
			case 6 :
				return Facility.LPR;
			case 7 :
				return Facility.NEWS;
			case 8 :
				return Facility.UUCP;
			case 9 :
				return Facility.CRON;
			case 10 :
				return Facility.AUTHPRIV;
			case 11 :
				return Facility.FTP;
			case 12 :
				return Facility.NTP;
			case 13 :
				return Facility.SECURITY;
			case 14 :
				return Facility.CONSOLE;
			case 16 :
				return Facility.LOCAL0;
			case 17 :
				return Facility.LOCAL1;
			case 18 :
				return Facility.LOCAL2;
			case 19 :
				return Facility.LOCAL3;
			case 20 :
				return Facility.LOCAL4;
			case 21 :
				return Facility.LOCAL5;
			case 22 :
				return Facility.LOCAL6;
			case 23 :
				return Facility.LOCAL7;
			}
			return null;
		}

		/**
		 * syslogヘッダの<PRI>から該当するSeverity値を返すAPI
		 * @param pri syslogヘッダの<PRI>部の値
		 * @return Severity値
		 */
		public static Severity getSeverity(int pri) {
			int severity = (pri & 0x00000007);

			switch (severity) {
			case 0 :
				return Severity.EMERG;
			case 1 :
				return Severity.ALERT;
			case 2 :
				return Severity.CRIT;
			case 3 :
				return Severity.ERR;
			case 4 :
				return Severity.WARNING;
			case 5 :
				return Severity.NOTICE;
			case 6 :
				return Severity.INFO;
			case 7 :
				return Severity.DEBUG;
			}

			return null;
		}


		public static void main(String args[]) throws Exception {

			String syslog1 = "<34>Oct 11 22:14:15 mymachine su: 'su root' failed for lonvick on /dev/pts/8";

			System.out.println(SyslogMessage.parse(syslog1));

		}

}
