/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.util.HinemosTime;

/**
 * このファイルは、マネージャとエージェントで複製されます。各々で特有の実装を追加することは、禁止です。
 * しかしながら、内部で利用する CalendarDetailInfo、CalendarInfo および  CalendarPatternInfo は、
 * マネージャとエージェントで各々の実装が存在します。
 * マネージャあるいは、エージェントから、相手に複製する際は、複製後にコンパイルが通過することを確認してください。
 * 今後、このファイルは、ant などで、マネージャとエージェントが必ず同じ内容する自動での対処が検討されています。
 * 
 */
public class CalendarUtil {
	private static Log m_log = LogFactory.getLog( CalendarUtil.class );

	private static final long HOUR0 = 0l;
	private static final long HOUR24 = 24 * 60 * 60 * 1000;
	
	/**
	 * カレンダ詳細設定優先順序単体試験用
	 * @param info
	 * @param date
	 * @return
	 */
	public static Integer isRunOrder(CalendarInfo info, Date date) {
		// 有効期間外の場合はfalse
		if (date.getTime() < info.getValidTimeFrom() ||
				info.getValidTimeTo() < date.getTime()) {
			return -1;
		}
		//カレンダ詳細定義と一致するか
		m_log.trace("DetailInfo=" + info.getCalendarDetailList().size());
		int count = 1;
		for (CalendarDetailInfo detailInfo : info.getCalendarDetailList()) {
			if (isRunByDetailDateTime(detailInfo,date)) {
				m_log.trace("CalendarID=" + info.getCalendarId() + "isRun");
				return count;
			}
			count++;
		}
		return -1;
	}


	/**
	 * 指定された日付が稼動/非稼動かを返します。
	 * 
	 * @param info
	 * @param date
	 * @return true:稼動、false:非稼動
	 */
	public static Boolean isRun(CalendarInfo info, Date date) {
		boolean ret = (Boolean)getCalendarRunDetailInfo(info, date, new ArrayList<CalendarDetailInfo>())[0];
		return ret;
	}

	/**
	 * 指定された日付に適応するCalendarDetailInfoのリストを引数のretDetailListに入れます。<br>
	 * 指定日が稼動ならtrue、非稼動ならfalseを返します。<br>
	 * 
	 * @param info
	 * @param date
	 * @return
	 */
	public static Object[] getCalendarRunDetailInfo(CalendarInfo info, Date date, ArrayList<CalendarDetailInfo> retDetailList) {
		if (info == null) {
			return new Object[]{true}; // カレンダが設定されていない場合はtrue
		}
		m_log.trace("Valid_START_Time : " + new Date(info.getValidTimeFrom()));
		m_log.trace("Valid_END_Time : " + new Date(info.getValidTimeTo()));
		m_log.trace("This_Time : " + date);

		Long timeFrom = info.getValidTimeFrom();
		Long timeTo = info.getValidTimeTo();
		// 有効期間外の場合はfalse
		if (date.getTime() < timeFrom || timeTo < date.getTime()) {
			return new Object[]{false};
		}
		
		for (CalendarDetailInfo detailInfo3 : info.getCalendarDetailList()) {
			// 現在の日付にhitする詳細情報があるなら、稼動情報を返す
			if (isRunByDetailDateTime(detailInfo3, date)) {
				m_log.trace("振り替え情報前にhitした description:" + detailInfo3.getDescription() + ", operationFlg:" + detailInfo3.isOperateFlg());
				m_log.trace("CalendarDetailInfo.toString = " + detailInfo3.toString());
				retDetailList.add(detailInfo3);
				if (detailInfo3.isOperateFlg()) {
					return new Object[]{true}; // カレンダが設定されていない場合はtrue
				} else {
					return new Object[]{false};
				}
			}
			if (detailInfo3.isSubstituteFlg() && detailInfo3.isOperateFlg()) {
				m_log.trace("振り替え情報があった description:" + detailInfo3.getDescription());
				boolean findhikadou = false;
				for (CalendarDetailInfo detailInfo : info.getCalendarDetailList()) {
					if (!detailInfo.isSubstituteFlg()) {
						continue;
					}
					for (int limit = 1; limit <= detailInfo.getSubstituteLimit(); limit++) {
						Date substituteDate = new Date(date.getTime() - (parseDate(detailInfo.getSubstituteTime()) + HinemosTime.getTimeZoneOffset()) *limit);
						m_log.trace("SubstituteDate:" + substituteDate + ", description:" + detailInfo.getDescription() + ", limit:" + limit);
						for (CalendarDetailInfo detailInfo2 : info.getCalendarDetailList()) {
							m_log.trace("CalendarDetailInfo.toString = " + detailInfo2.toString());
							if (detailInfo.equals(detailInfo2)) {
								if (!findhikadou) {
									m_log.trace("非稼動チェックでヒットしなかった、振り替えない。return false.");
									return new Object[]{false};
								} else {
									if (!isRunByDetailDateTime(detailInfo2, substituteDate)) {
										m_log.trace("対象日は非稼動だ、振り替えにはヒットしなかった。次の候補日チェックする。フラグおろす。break.");
										retDetailList.clear();
										findhikadou = false;
										break;
									} else {
										if (substituteDate.getTime() < timeFrom || timeTo < substituteDate.getTime()) {
											m_log.trace("対象日は非稼動だ、振り替えにもhitした、でも振替日は有効期間外 return false.");
											return new Object[]{false};
										}
										m_log.trace("対象日は非稼動だ、振り替えにもhitした、振り替える return true.");
										retDetailList.add(detailInfo2);
										m_log.trace("substituteDate:" + substituteDate.toString());
										Date subRetDate = new Date(substituteDate.getTime());
										return new Object[]{true, subRetDate};
									}
								}
							}
							if (isRunByDetailDateTime(detailInfo2, substituteDate) && !detailInfo2.isOperateFlg()) {
								m_log.trace("対象日は非稼動だ、振り替えあるかチェックしよう...フラグ立てる");
								findhikadou = true;
							}
						}
					}
				}
			}
		}
		m_log.trace("何にもhitしない、非稼動。 return false. calendarId:" + info.getCalendarId());
		return new Object[]{false};
	}

	/**
	 * カレンダ詳細定義 - 年、月、日、時間、分、秒が現在の時間が等しいか調べる
	 * 
	 * detailInfoのtimeFrom,timeToには0-48時が入っている。
	 * @param detailInfo
	 * @param date 現在時刻
	 * @return
	 */
	public static boolean isRunByDetailDateTime(CalendarDetailInfo detailInfo, Date date) {
		for (CalendarDetailInfo detail : getDetail24(detailInfo)) {
			if (isRunByDetailDate(detail, date) && isRunByDetailTime(detail, date)) {
				m_log.trace("True : carry out " + detail.getDescription());
				return true;
			}
		}
		return false;
	}

	/**
	 * detailInfoのtimeFrom,timeToには0-24時が入っている。
	 * @param detailInfo
	 * @param date 現在時刻
	 * @return
	 */
	public static boolean isRunByDetailDate(CalendarDetailInfo detailInfo, Date date) {
		// from, toが0-24時間以内であることを確認する。
		long timezoneOffset = HinemosTime.getTimeZoneOffset();
		long hour24 = 24*60*60*1000 - timezoneOffset;

		if (detailInfo.getTimeFrom() < (0 - timezoneOffset) || hour24 < (detailInfo.getTimeFrom())) {
			m_log.trace("detailInfo.getTimeFrom = " + detailInfo.getTimeFrom());
			return false;
		}
		if ((detailInfo.getTimeTo()) < (0 - timezoneOffset) || hour24 < (detailInfo.getTimeTo())) {
			m_log.trace("detailInfo.getTimeTo = " + (detailInfo.getTimeTo()) );
			return false;
		}

		//nullチェック
		if(detailInfo.getMonth() == null){
			m_log.warn("detailInfo.getMonth() is NULL");
			return false;
		}

		//カレンダ取得
		Calendar detailCal = HinemosTime.getCalendarInstance();
		//ｘ日後を考慮したカレンダ取得
		detailCal.setTime(new Date(date.getTime() - (long)detailInfo.getAfterday() * 24 * 3600 * 1000));
		//年
		int year = detailCal.get(Calendar.YEAR);
		//月
		int month = detailCal.get(Calendar.MONTH)+1;
		//週（第ｘ週）
		int weekXth = detailCal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
		//曜日
		int weekday = detailCal.get(Calendar.DAY_OF_WEEK);
		//日
		int dateNo = detailCal.get(Calendar.DATE);
		
		m_log.debug("detailCal=" + detailCal.getTime());

		/**
		 * 年
		 */
		//カレンダの年 と 現在の年が等しくない場合（0は毎年）
		if(detailInfo.getYear() != year && detailInfo.getYear() != 0){
			m_log.trace("Year is false : Schdule year =" + detailInfo.getYear()
					+ ", Current Year =" + year);
			return false;
		}
		m_log.trace("Year is TRUE : Schduler Year =" + detailInfo.getYear()
				+ ", Current Year =" + year);

		/**
		 * 月
		 */
		//カレンダの月 と 現在の月が等しくない場合（0は毎月）
		if(detailInfo.getMonth() != month && detailInfo.getMonth() != 0){
			m_log.trace("Month is False : Schduler Month =" + detailInfo.getMonth()
					+ ", Current Month =" + month);
			return false;
		}
		m_log.trace("Month is TRUE : Schduler Month =" + detailInfo.getMonth()
				+ ", Current Month =" + month);

		/**
		 * 日
		 */
		//nullチェック
		if(detailInfo.getDayType() == null){
			m_log.warn("detailInfo.getDayType() is NULL");
			return false;
		}
		//カレンダの週、曜日、日 と 現在の日が等しいか
		//すべての日
		if(detailInfo.getDayType() == 0){
			m_log.trace("DateALL is TRUE : Schduler dayType=" + detailInfo.getDayType()
					+ ", Current Date =" + dateNo);
			return true;
		}
		//曜日を指定した場合
		else if(detailInfo.getDayType() == 1){
			//nullチェック
			if(detailInfo.getDayOfWeekInMonth() == null ){
				m_log.trace("detailInfo.getDayOfWeekInMonth() is NULL");
				return false;
			}
			//nullチェック
			if(detailInfo.getDayOfWeek() == null ){
				m_log.warn("detailInfo.getDayOfWeek() is NULL");
				return false;
			}
			if(detailInfo.getDayOfWeekInMonth() == weekXth || detailInfo.getDayOfWeekInMonth() == 0){
				m_log.trace("WeekXth is TRUE : Schduler Xth =" + detailInfo.getDayOfWeekInMonth()
						+ ", Current weekXth =" + weekXth);
				if(detailInfo.getDayOfWeek() == weekday){
					m_log.trace("Weekday is TRUE : Schduler Weekday =" + detailInfo.getDayOfWeek()
							+ ", Current Weekday =" + weekday);
					return true;
				}
			}
		}
		//日 を指定した場合
		else if(detailInfo.getDayType() == 2){
			//nullチェック
			if(detailInfo.getDate() == null){
				m_log.trace("detailInfo.getDate() is NULL");
				return false;
			}
			if(detailInfo.getDate() == dateNo){
				m_log.trace("Date is TRUE : Schduler Date =" + detailInfo.getDate()  + ", Current Date =" + dateNo);
				return true;
			}
		}
		//カレンダパターンを指定した場合
		else if(detailInfo.getDayType() == 3){
			//nullチェック
			if(detailInfo.getCalPatternId() == null){
				m_log.trace("detailInfo.getCalPatternId() is NULL");
				return false;
			}
			m_log.trace("CalendarPatternID = " + detailInfo.getCalPatternId());
			CalendarPatternInfo calPatternInfo = null;
			calPatternInfo = detailInfo.getCalPatternInfo();
			if (calPatternInfo.isRun(year, month, dateNo)) {
				m_log.trace(year + "/" + month + "/" + dateNo);
				return true;
			}
		}
		return false;
	}

	/**
	 * カレンダ詳細定義 - 時間 と現在の時間が等しいか調べる
	 * 
	 * @param detailInfo
	 * @param date
	 * @return 等しいなら True
	 */
	public static boolean isRunByDetailTime(CalendarDetailInfo detailInfo, Date date) {
		//開始、終了 時間 (00:00:00 - 24:00:00)
		long timezoneOffset = HinemosTime.getTimeZoneOffset();
		//nullチェック
		if(detailInfo.getTimeFrom() == null || detailInfo.getTimeTo() == null){
			m_log.warn("detailInfo.getTime is NULL");
			return false;
		}

		//現在の日にちの00:00:00取得
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		sdf.setTimeZone(HinemosTime.getTimeZone());
		String strNow = sdf.format(date);
		Date dateNow = null;
		try {
			dateNow = sdf.parse(strNow);
			/*
			 * カレンダ詳細より取得した、開始、終了時間は、1970/1/1 の日時で保持されている。
			 * そのため、詳細ダイアログにて設定した年月日の開始、終了時間になるよう変換する。
			 */
			long from = detailInfo.getTimeFrom() + timezoneOffset + dateNow.getTime();
			long to = detailInfo.getTimeTo() + timezoneOffset + dateNow.getTime();
			m_log.trace("this Time       " + date);
			m_log.trace("DetailTimeFrom  " + new Date(from));
			m_log.trace("DetailTimeTo    " + new Date(to));

			//現在の時刻取得（Long型）
			Long checkTime = date.getTime();

			//現時刻が設定した開始時間から終了時間内か
			if(from <= checkTime && checkTime < to){
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// 有効期間内だが、詳細定義の日程に含まれない場合
		return false;
	}

	/**
	 * CalendarDetailInfoの時刻を整理するメソッド
	 * 
	 * detailInfoのtimeFrom,timeToには0時未満や48時超の値が入ることもある。
	 * @param detailInfo
	 * @param detailInfo
	 * @param date
	 * @return 引数のdetailInfoを0～24時内に分割したdetailInfoのリスト
	 */
	public static ArrayList<CalendarDetailInfo> getDetail24(CalendarDetailInfo detailInfo) {
		ArrayList<CalendarDetailInfo> ret = new ArrayList<CalendarDetailInfo>();

		long timezoneOffset = HinemosTime.getTimeZoneOffset();
		long from = detailInfo.getTimeFrom() + timezoneOffset;
		long to = detailInfo.getTimeTo() + timezoneOffset;
		
		// from及びtoの値が0～24(0)や24～48(1)、-24～0(-1)等のどの範囲内かで補正値(←の括弧内の値)を算出する。
		// 補正値が何日前または何日後の値になるかを示し、setAfterday()への設定値、
		// 及びsetTimeFrom()、setTimeTo()に設定する際の加算値、減算値(24時間単位)算出の基準値とする。
		// 24時や48時等、区切りの時間の場合は、余計な分割させないように、1日前(負数の場合は1日後)相当の補正値にする。
		long correctFrom = 0 <= from ? from/HOUR24 : (from+1)/HOUR24-1;
		long correctTo = 0 <= to ? (to-1)/HOUR24 : to/HOUR24-1;
		
		if(HOUR0 <= from && from <= HOUR24 && HOUR0 <= to && to <= HOUR24) {
			/*
			 * 分割も日時補正も不要なケース(分割済データがパラメタ指定された場合含む)
			 * 
			 * from及びtoがともに0時以上、24時以内の場合(0 <= from & to <= 24)。
			 * info = 06:00 ～ 18:00 (当日)
			 */
			m_log.trace("0 <= from & to <= 24");
			ret.add(detailInfo);
		}
		else if(correctFrom == correctTo){
			/*
			 * 分割不要、日時補正が必要なケース(補正例を以下に記載)
			 * 
			 * from及びtoがともに-24時以上、0時以内の場合(-24 <= from & to <= 0)。
			 * info  =-12:00 ～-06:00 (当日)
			 * info1 = 12:00 ～ 18:00 (前日)
			 * 
			 * from及びtoがともに24時以上、48時以内の場合(24 <= from & to <= 48)。
			 * info  = 30:00 ～ 36:00 (当日)
			 * info1 = 06:00 ～ 12:00 (翌日)
			 * 
			 * from及びtoがともに48時以上、72時以内の場合(48 <= from & to <= 72)。
			 * info  = 54:00 ～ 60:00 (当日)
			 * info1 = 06:00 ～ 12:00 (翌々日)
			 */
			m_log.trace("correctFrom = correctTo:" + correctFrom);
			CalendarDetailInfo info1 = detailInfo.clone();
			info1.setAfterday(detailInfo.getAfterday() + (int)correctFrom);
			info1.setTimeFrom(info1.getTimeFrom() - HOUR24*correctFrom);
			info1.setTimeTo(info1.getTimeTo() - HOUR24*correctTo);
			ret.add(info1);
		} else {
			/*
			 * 2分割、かつ日時補正が必要なケース(補正例を以下に記載)
			 * 
			 * fromが-24時以上かつ0時未満で、toが0時超えかつ24時以下の場合(-24 <= from < 0 < to <= 24)。
			 * info  =-06:00 ～ 06:00 (当日)
			 * info1 = 18:00 ～ 24:00 (前日)
			 * info2 = 00:00 ～ 06:00 (当日)
			 * 
			 * fromが0時以上かつ24時未満で、toが24時超えかつ48時以下の場合(0 <= from < 24 < to <= 48)。
			 * info =  18:00 ～ 30:00 (当日)
			 * info1 = 18:00 ～ 24:00 (当日)
			 * info2 = 00:00 ～ 06:00 (翌日)
			 * 
			 * fromが24時以上かつ48時未満で、toが48超えかつ72時以下の場合(24 <= from < 48 < to <= 72)。
			 * info  = 30:00 ～ 54:00 (当日)
			 * info1 = 06:00 ～ 24:00 (翌日)
			 * info2 = 00:00 ～-06:00 (翌々日)
			 */
			/*
			 * 3分割以上、かつ日時補正が必要なケース(補正例を以下に記載)
			 * 
			 * fromが-24時以上かつ0時未満で、toが24時超えかつ48時以下の場合(-24 <= from < 0 < 24 < to <= 48)。
			 * info  =-06:00 ～ 30:00 (当日)
			 * info1 = 18:00 ～ 24:00 (前日)
			 * tmp   = 00:00 ～ 24:00 (当日)
			 * info2 = 00:00 ～ 06:00 (翌日)
			 * 
			 * fromが0時以上かつ24時未満で、toが48超えかつ72時以下の場合(0 <= from < 24 < 48 < to <= 72)。
			 * info  = 18:00～ 54:00 (当日)
			 * info1 = 18:00～-24:00 (当日)
			 * tmp   = 00:00～-24:00 (翌日)
			 * info2 = 00:00～-06:00 (翌々日)
			 */
			m_log.trace("correctFrom:" + correctFrom + "～correctTo:" + correctTo);
			
			CalendarDetailInfo info1 = detailInfo.clone();
			CalendarDetailInfo info2 = detailInfo.clone();
			
			info1.setAfterday(detailInfo.getAfterday() + (int)correctFrom);
			info1.setTimeFrom(info1.getTimeFrom() - HOUR24*correctFrom);
			info1.setTimeTo(HOUR24 - timezoneOffset);
			ret.add(info1);
			
			if(correctTo - correctFrom > 1){
				// 3分割以上の場合、tmp作成(0～24となるデータを必要日数分作成)
				for(long i = correctFrom + 1; i < correctTo; i++){
					CalendarDetailInfo tmp = detailInfo.clone();
					tmp.setAfterday(detailInfo.getAfterday() + (int)i);
					tmp.setTimeFrom(HOUR0 - timezoneOffset);
					tmp.setTimeTo(HOUR24 - timezoneOffset);
					ret.add(tmp);
				}
			}
			
			info2.setAfterday(detailInfo.getAfterday() + (int)correctTo);
			info2.setTimeFrom(HOUR0 - timezoneOffset);
			info2.setTimeTo(info2.getTimeTo() - HOUR24*correctTo);
			ret.add(info2);
		}
		
		return ret;
	}

	/**
	 * hourからDateを生成してlongで返します。(HinemosClientのTimeStringComverterを参考)
	 * @param hour
	 * @return
	 */
	public static long parseDate(int hour) {
		long msecTime = 1000*(hour*3600);
		Date dateTime = new Date(msecTime - HinemosTime.getTimeZoneOffset());
		return dateTime.getTime();
	}
}
