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
import java.text.SimpleDateFormat;
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
	// システムログをチェックする年数
	private static final int LEAPYEAR_CHECK_COUNT = 7;
	
	// Syslog TAG部の終端記号
	private static final String SYSLOG_TAG_TERM_MARK = ":";
	
	private static Log log = LogFactory.getLog(SyslogMessage.class);

	public Facility facility;
	public Severity severity;
	public long date;
	public String hostname;
	public String message;
	public String rawSyslog;
	public String ipAddress;

	// 外部依存処理
	static External external = new External();
	static class External {
		int getMonitorSystemlogMonitorPeriodHour() {
			return HinemosPropertyCommon.monitor_systemlog_period_hour.getIntegerValue();
		}
	}
	
	public enum Facility { KERN, USER, MAIL, DAEMON, AUTH, SYSLOG, LPR, NEWS, UUCP, CRON, AUTHPRIV, FTP, NTP, SECURITY, CONSOLE,
		LOCAL0, LOCAL1, LOCAL2, LOCAL3, LOCAL4, LOCAL5, LOCAL6, LOCAL7 };

		public enum Severity { EMERG, ALERT, CRIT, ERR, WARNING, NOTICE, INFO, DEBUG };

		public SyslogMessage() {
		}

		public SyslogMessage(Facility facility, Severity severity, long date, String hostname, String message, String rawSyslog, String ipAddress) {
			this.facility = facility;
			this.severity = severity;
			this.date = date;
			this.hostname = hostname;
			this.message = message;
			this.rawSyslog = rawSyslog;
			this.ipAddress = ipAddress;
		}

		/**
		 * syslogの文字列をパースし、その解析結果をSyslogMessageインスタンスとして返すAPI<br>
		 * 
		 * RFC3164に基づいて動作する。RFC5424はRFC3164と互換性が低いので対応されない。
		 * 
		 * 以下の形式に対応している
		 * 1.<PRI>DATE HOSTNAME TAG MSG -> message には TAG MSGがセットされる
		 * 2.<PRI>DATE TAG MSG          -> hostnameには 送信元IPが設定される。
		 * 3.<PRI>TAG MSG               -> dateには システム時刻が設定される。
		 * 4.TAG MSG(もしくは破損データ)-> facilityには user,severityにはnoticeがセットされる。
		 * 
		 * DATEの有無について RFC3164定義もしくRFC3339定義の形式であれば有としている。
		 * 
		 * HOSTNAMEの有無については 文字列チェックを行い、HOSTNAMEとして適切な文字列の場合のみ有としている。
		 * 
		 * TAGとHOSTNAMEの区別については TAGの末尾に入る : の有無で判断している。
		 * 
		 * @param syslog syslogの文字列
		 * @param senderAddress 送信者アドレス
		 * @return SyslogMessageインスタンス
		 * @throws ParseException syslogのフォーマットに従っていない文字列が与えられた場合
		 * @throws HinemosUnknown 
		 */
		public static SyslogMessage parse(String syslog, String senderAddress) throws ParseException, HinemosUnknown {
			StringBuffer readBuf = new StringBuffer(syslog);
			if (log.isDebugEnabled()) {
				log.debug("parsed start syslog : " + syslog + ",senderAddress=" +senderAddress);
			}

			// InetAddress#toString() の返す "ホスト名/IPアドレス" 形式の場合、IPアドレス部分のみに変換
			String addr[] = senderAddress.split("/");
			senderAddress = (addr.length == 2) ? addr[1] : addr[0];

			//PRI取得（取得された場合、該当箇所はreadBufから切除）
			Integer parsePri =getSyslogHeaderPRI(readBuf);
			if(parsePri==null){
				int pri =13;
				SyslogMessage instance = new SyslogMessage(getFacility((int) pri), getSeverity((int) pri), System.currentTimeMillis(), senderAddress, readBuf.toString(), syslog, senderAddress);
				if (log.isDebugEnabled()) {
					log.debug("parsed syslog : " + instance);
				}
				return instance;
			}

			//DATE取得（取得された場合、該当箇所はreadBufから切除）
			Date parseDate = getSyslogHeaderDate(readBuf);
			if(parseDate == null){
				SyslogMessage instance = new SyslogMessage(getFacility((int) parsePri), getSeverity((int) parsePri), System.currentTimeMillis(), senderAddress, readBuf.toString(), syslog, senderAddress);
				if (log.isDebugEnabled()) {
					log.debug("parsed syslog : " + instance);
				}
				return instance;
			}
			
			//HOSTNAME取得（取得された場合、該当箇所はreadBufから切除）
			String parseHost =getSyslogHeaderHost(readBuf);
			if(parseHost == null){
				parseHost = senderAddress;
			}

			//残ったreadBufはTAG MSGとして扱う
			// インスタンスの生成
			SyslogMessage instance = new SyslogMessage(getFacility((int) parsePri), getSeverity((int) parsePri), parseDate.getTime(), parseHost, readBuf.toString(), syslog, senderAddress);
			if (log.isDebugEnabled()) {
				log.debug("parsed syslog : " + instance);
			}
			
			return instance;
		}
		/**
		 * 
		 * SYSLOG(RFC3164) PRI部 取得  
		 * 
		 * parseメソッドからの からの呼び出し専用
		 * 
		 * @param readBuf 読み込み文字. 取得成功したら 該当部分は削除して返却
		 * @return Integer PRI値
		 */
		private static Integer getSyslogHeaderPRI(final StringBuffer readBuf ) {
			//headerおよびmessageが存在しない場合
			if ( readBuf == null || readBuf.length() == 0 ){
				log.debug("getSyslogHearderPRI(): syslog_buf is either null or zero. No header and/or message is provided");
				return null;
			}
			
			if ( readBuf.charAt(0) != '<' ){
				return null;
			} 
			int readCnt ;
			try{
			for( readCnt = 1; readCnt < 5 ;readCnt++ ){
				if( readCnt > 1 && readBuf.charAt(readCnt) == '>' ){
						Integer ret = Integer.valueOf(readBuf.substring(1, readCnt ));
						readBuf.delete(0, readCnt+1);
						return ret;
					}
				}
			}catch(Exception e){
				return null;
			}
			return null;
		}

		/**
		 * 
		 * SYSLOG(RFC3164) DATE部取得 
		 * 
		 * parseメソッドからの からの呼び出し専用
		 * 
		 * <br>
		 * <b>システムログの年算出方法の概要</b><br>
		 * 1. システム日付の年の前年、当年、翌年とシステムログ日付の月日時分ミリ秒を組み合わせた３パターンの年月日を用意する<br>
		 * 2. 現在日付と３パターンの時間差を算出し最も時間差の小さい年をシステムログの年とする<br>
		 * 
		 * @param readBuf 読み込み文字. 取得成功したら 該当部分は削除して返却
		 * @return DATE部 parse値
		 */
		private static Date getSyslogHeaderDate(final StringBuffer readBuf) {

			Date RetDate = null;
			boolean isRfc3164 = false;
			boolean notRfc13164 = false;
			boolean isCiscoHead = false;
			Object[] syslogArgs = null;
			
			//文字数不足チェック
			if(readBuf.length() < 4){
				return null;
			} 
			//先頭の4文字 形式チェック"MMM "形式(当てはまらないなら Rfc13164 形式にあらず)
			if( ( readBuf.substring(3,4).equals(" ") == false ) || editCalendarMonth(readBuf.substring(0,3)) == Calendar.UNDECIMBER ){
				notRfc13164 =true;
			}
			//Rfc13164でない場合はRFC3339チェックのみ
			if (notRfc13164){
				try {
					final MessageFormat syslogFormatUtil_1 = new MessageFormat("{0} {1}", Locale.ENGLISH);
					syslogArgs = syslogFormatUtil_1.parse(readBuf.toString());
					RetDate = parseRFC3339Date((String)syslogArgs[0]);
				} catch (ParseException e3) {
					if (log.isDebugEnabled()) {
						log.debug("ParseException3 : message=" + e3.getMessage() + ", syslog_buf=" + readBuf.toString());
					}
				}
				if(RetDate!=null){
					readBuf.delete(0, readBuf.length());
					readBuf.append((String)syslogArgs[1]);
				}
				return RetDate;
			}

			if(notRfc13164==false && readBuf.substring(3,5).equals("  ")  ){
				//Rfc13164の可能性があるなら ddの先頭が ブランクの場合( 本来 "01" だが " 1" のときがある  )向けの補正
				readBuf.delete(4, 5);
			}
		
			// RFC3164の標準の日付形式でのフォーマット取得
			// [0]:月(MMM), [1]:日(dd), [2]:時(HH), [3]:分(mm), [4]:秒(ss) [5]残り
			// システムログの時間フォーマットを{1,date,MMM dd HH:mm:ss}で取得した場合、年指定がないため、1970年として扱われる。
			// その結果、システムログ日付が閏年で2/29の場合、3/1に置き換えられる。(1970年は閏年でないため)

			// 文字列をパースしてヘッダ情報およびメッセージを取得する
			if (isRfc3164 == false && notRfc13164 == false ){
				try {
					final MessageFormat syslogDateFormatRFC3164_1 = new MessageFormat("{0} {1,number,integer} {2,number,integer}:{3,number,integer}:{4,number,integer} {5}", Locale.ENGLISH);
					syslogArgs = syslogDateFormatRFC3164_1.parse(readBuf.toString());
					isRfc3164 = true;
				} catch (ParseException e1) {
					if (log.isDebugEnabled()) {
						log.debug("ParseException1 : message=" + e1.getMessage() + ", syslog_buf=" + readBuf);
					}
				}
			}

			//パースできなかった場合はCISCOの特殊形式(MMM dd yyyy HH:mm:ss) を試行
			// [0]:月(MMM), [1]:日(dd), [2]:年(yyyy), [3]:時(HH), [4]:分(mm), [5]:秒(ss) [6]残り
			if (isRfc3164 == false && notRfc13164 == false ){
				try {
					final MessageFormat syslogDateFormatRFC3164_2 = new MessageFormat("{0} {1,number,integer} {2,number,integer} {3,number,integer}:{4,number,integer}:{5,number,integer} {6}", Locale.ENGLISH);
					syslogArgs = syslogDateFormatRFC3164_2.parse(readBuf.toString());
					isCiscoHead =true;
				} catch (ParseException e2) {
					if (log.isDebugEnabled()) {
						log.debug("ParseException2 : message=" + e2.getMessage() + ", syslog_buf=" + readBuf.toString());
					}
				}
			}
			//RFC3164とCISCOどちらでもないならＮＧ
			if ( isRfc3164==false && isCiscoHead == false ){
				return null;
			}
			
			if (log.isDebugEnabled()) {
				int i = 0;
				for (Object arg : syslogArgs) {
					log.debug(String.format("date args [%d] : %s", i++, arg.toString()));
				}
			}
			//CISCO形式時の対応
			if(isCiscoHead == true){
				int year =Integer.parseInt(((Long) syslogArgs[2]).toString());
				int month = editCalendarMonth((String) syslogArgs[0]);
				int dayOfMonth = Integer.parseInt(((Long) syslogArgs[1]).toString());
				int hourOfDay = Integer.parseInt(((Long) syslogArgs[3]).toString());
				int minute = Integer.parseInt(((Long) syslogArgs[4]).toString());
				int second = Integer.parseInt(((Long) syslogArgs[5]).toString());
				Calendar editSyslogCal = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
				readBuf.delete(0, readBuf.length());
				readBuf.append((String)syslogArgs[6]);
				return editSyslogCal.getTime();
			}
			
			//rfc13164形式日付の後続処理 年の指定がないので候補を推定して決定
			// システムログ時刻が現在時刻より未来の場合に許容する時間(H) 
			Integer syslogEffectiveTime = external.getMonitorSystemlogMonitorPeriodHour();

			// 0以下が設定された場合は、デフォルト値に置き換え
			if (syslogEffectiveTime <= 0) {
				syslogEffectiveTime = SYSLOG_DEFAULT_PERIOD_HOUR;
			}

			Calendar nowCal = HinemosTime.getCalendarInstance();

			// 翌年でシステムログ年月日を生成(年跨ぎを考慮)
			int year = nowCal.get(Calendar.YEAR) + 1;
			int month = editCalendarMonth((String) syslogArgs[0]);
			int dayOfMonth = Integer.parseInt(((Long) syslogArgs[1]).toString());
			int hourOfDay = Integer.parseInt(((Long) syslogArgs[2]).toString());
			int minute = Integer.parseInt(((Long) syslogArgs[3]).toString());
			int second = Integer.parseInt(((Long) syslogArgs[4]).toString());

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
				log.warn("System log date is invalid : " + readBuf.toString());
			}
			
			readBuf.delete(0, readBuf.length());
			readBuf.append((String)syslogArgs[5]);
			return editSyslogCal.getTime();
			
		}

		/**
		 * 
		 * RFC3339形式 DATE解釈 
		 * 
		 * getSyslogHeaderDateメソッドからの からの呼び出し専用
		 * 
		 * @param parseStr 読み込み文字.成功しても削除はしない
		 * @return DATE部 parse値 不可能ならnull返却
		 */
		private static Date parseRFC3339Date(final String parseStr ) {
			final int dtPartSize =19;
			final int tzPartSizeMix =5;
			final int tzPartSizeMax =6;
			final int msPartSizeMax =7;

			// （日時＋タイムゾーン最小）未満  もしくは （日時＋ミリ秒最大＋タイムゾーン最大）超過以上ならＮＧ
			if( parseStr.length() < (dtPartSize + tzPartSizeMix ) || (dtPartSize + tzPartSizeMax + msPartSizeMax ) <  parseStr.length() ){
				return null;
			}
			
			//時刻部分チェック
			// 文字列をパースしてヘッダ情報およびメッセージを取得する
			final MessageFormat syslogFormat1 = new MessageFormat("{0}-{1}-{2}T{3}:{4}:{5}", Locale.ENGLISH);
			Object[] DtArgs = null;
			try {
				DtArgs = syslogFormat1.parse(parseStr.substring(0,19));
			} catch (ParseException e1) {
				if (log.isDebugEnabled()) {
					log.debug("parseRFC3339Date ParseException1 : message=" + e1.getMessage() + ", str=" + parseStr);
				}
				return null;
			}

			String tzPlMn ; //タイムゾーン + or - 部分
			String tzHour ;//タイムゾーン 時 部分
			String tzMins ;//タイムゾーン 分 部分

			//タイムゾーン指定開始位置を検索（見つからなければフォーマット外とする）
			int tzPos = 0; //タイムゾーン開始位置
			tzPos = parseStr.lastIndexOf("+");
			if( tzPos > -1 ){
				tzPlMn = "+";
			}else if(( tzPos = parseStr.lastIndexOf("-") ) > -1){
				tzPlMn = "-";
			}else{
				return null;
			}

			//ミリ秒指定部分があるならフォーマットチェック（ＮＧなら中止）
			final int msPartLength = tzPos - dtPartSize ;
			if( ( msPartLength ) !=  0 ){
				//先頭は.
				if( parseStr.charAt(dtPartSize) != '.' ){ 
					return null;
				}
				// 残りは数値
				for (int loopCnt=dtPartSize + 1;loopCnt < tzPos;loopCnt++ ){
					if( java.lang.Character.isDigit(parseStr.charAt(loopCnt)) == false ){
						return null;
					}
					
				}
			}
			
			//-or+以降の文字数不足チェック（ＮＧなら中止）
			if(  parseStr.length() < tzPos + 3 ){
				return null;
			}
			//タイムゾーンの数値(時と分)の取得
			final String tzStr = parseStr.substring(tzPos+1);
			if(tzStr.length() == 4){ 
				// +0900 形式
				tzHour = tzStr.substring(0,2);
				tzMins = tzStr.substring(2,4);
			}else if(tzStr.length() == 5 && tzStr.substring(2,3).equals(":")){ 
				// +09:00 形式
				tzHour = tzStr.substring(0,2);
				tzMins = tzStr.substring(3,5);
			}else{
				return null;
			}
			//SimpleDateFormatで解釈可能な形式に調整して変換
			String parseTag = (String) DtArgs[0] + (String) DtArgs[1]+ (String) DtArgs[2] +'T'+ (String) DtArgs[3]+ (String) DtArgs[4] + (String) DtArgs[5] 
					+ tzPlMn + tzHour +tzMins ;
			final SimpleDateFormat sdfIso8601BasicFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");
			try{
				Date date = sdfIso8601BasicFormat.parse(parseTag);
				return date;
			} catch (Exception e2) {
				if (log.isDebugEnabled()) {
					log.debug("parseRFC3339Date ParseException2 : message=" + e2.getMessage() + ", str=" + parseStr);
				}
			}
			return null;
		}

		/**
		 * 
		 * SYSLOG(RFC3164) HOSTNAME部取得 
		 * 
		 * parseメソッドからの からの呼び出し専用
		 * 
		 * @param readBuf 読み込み文字. 取得成功したら 該当部分は削除して返却
		 * @return DATE部 parse値
		 */
		private static String getSyslogHeaderHost(final StringBuffer readBuf ) {
			Object[] syslogArgs;
			//最初のブランクまで（HOSTNAME候補）と以後の文字を分割します。
			try {
				final MessageFormat syslogFormat = new MessageFormat("{0} {1}", Locale.ENGLISH);
				syslogArgs = syslogFormat.parse(readBuf.toString());
			} catch (ParseException e1) {
				if (log.isDebugEnabled()) {
					log.debug("getSyslogHeaderHost ParseException1 : message=" + e1.getMessage() + ", str=" + readBuf);
				}
				return null;
			}

			String hostname =(String) syslogArgs[0];
			String msg = (String)  syslogArgs[1];
			
			//hostnameチェックして、おかしい場合はメッセージの一部として扱います。
			//特定の形式の場合、補完します

			//  末尾 ":" チェック    . <PRI>DATE TAG MSG形式（主に商用Unix）を想定
			//  ブランク チェック . <PRI>DATE  last message repeat を想定
			//  [ ] 囲い込み補正  .特定のsyslogdにてホスト名を[]にて囲う仕様に対応
			if( hostname.equals("") ||
				hostname.substring( hostname.length() - 1 ).equals(SYSLOG_TAG_TERM_MARK) ){
				return null;
			}else if(hostname.substring( 0,1 ).equals("[") && hostname.substring( hostname.length() - 1 ).equals("]") ){
				hostname = hostname.substring( 1 , hostname.length()- 1 );
			}
			
			//ホスト名許容文字チェック 半角英数 . / - _ であること 
			for (int cnt =0 ; cnt <hostname.length(); cnt++){
				if(Character.isDigit(hostname.charAt(cnt)) 
				|| Character.isUpperCase(hostname.charAt(cnt)) 
				|| Character.isLowerCase(hostname.charAt(cnt))
				|| hostname.charAt(cnt) == '.' || hostname.charAt(cnt) == '/' || hostname.charAt(cnt) == '_' || hostname.charAt(cnt) == '-'){
					continue;
				}
				return null;
			}
			
			readBuf.delete(0, readBuf.length());
			readBuf.append(msg);
			return hostname;
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
			return String.format("%s [facility = %s, severity = %s, date = %s, hostname = %s, ipaddr = %s, message = %s]",
					this.getClass().getSimpleName(), facility, severity, new Date(date), hostname, ipAddress, message);
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
			case 15 :
				return Facility.CRON;
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

			System.out.println(SyslogMessage.parse(syslog1,"127.0.0.1"));

		}

}
