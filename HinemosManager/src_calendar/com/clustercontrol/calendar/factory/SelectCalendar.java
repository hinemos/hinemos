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

package com.clustercontrol.calendar.factory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.calendar.model.YMD;
import com.clustercontrol.calendar.util.CalendarCache;
import com.clustercontrol.calendar.util.CalendarPatternCache;
import com.clustercontrol.calendar.util.CalendarUtil;
import com.clustercontrol.calendar.util.QueryUtil;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.HinemosTime;


/**
 * カレンダを検索するファクトリークラス<BR>
 *
 * @version 4.1.0
 * @since 2.0.0
 */
public class SelectCalendar {

	private static Log m_log = LogFactory.getLog( SelectCalendar.class );

	private static final long TIMEZONE = HinemosTime.getTimeZoneOffset();
	private static final long HOUR24 = 24 * 60 * 60 * 1000;

	/**
	 * カレンダ情報をキャッシュより取得します。
	 * 
	 * @param id
	 * @return カレンダ情報
	 * @throws CalendarNotFound
	 */
	public CalendarInfo getCalendarFromCache(String id) throws CalendarNotFound {
		CalendarInfo ret = null;
		if(id != null && !"".equals(id)){
			ret = CalendarCache.getCalendarInfo(id);
		}
		return ret;
	}

	/**
	 * カレンダ情報を取得します。
	 * 
	 * @param id
	 * @return カレンダ情報
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public CalendarInfo getCalendar(String id) throws CalendarNotFound, InvalidRole {
		CalendarInfo ret = null;
		if(id != null && !id.isEmpty()){
			//カレンダ取得
			ret = QueryUtil.getCalInfoPK(id);
		}

		return ret;
	}

	/**
	 * カレンダ詳細情報一覧を取得します。
	 * @param id
	 * @return カレンダ詳細情報のリスト
	 */
	public ArrayList<CalendarDetailInfo> getCalDetailList(String id) {
		//カレンダIDの曜日別情報を取得
		return new ArrayList<>(QueryUtil.getCalDetailByCalendarId(id));
	}

	/**
	 * 
	 * @param calendarId
	 * @return
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public CalendarInfo getCalendarFull(String calendarId) throws CalendarNotFound, InvalidRole {
		CalendarInfo info = CalendarCache.getCalendarInfo(calendarId);
		if (info == null) {
			return null;
		}
		for (CalendarDetailInfo detail : info.getCalendarDetailList()) {
			String calPatternId = detail.getCalPatternId();
			if (calPatternId == null || calPatternId.length() == 0) {
				continue;
			}
			//キャッシュより取得する
			CalendarPatternInfo calPatternInfo = CalendarPatternCache.getCalendarPatternInfo(calPatternId);
			m_log.debug("getCalendarFull() : calPatternInfo=" + calPatternInfo);
			detail.setCalPatternInfo(calPatternInfo);
		}
		return info;
	}

	/**
	 * カレンダ情報一覧を取得します。
	 * 
	 * @return カレンダ情報のリスト
	 */
	public ArrayList<CalendarInfo> getAllCalendarList(String ownerRoleId) {
		List<CalendarInfo> ct = null;
		if (ownerRoleId == null || ownerRoleId.isEmpty()) {
			//全カレンダを取得
			ct = QueryUtil.getAllCalInfo();
		} else {
			// オーナーロールIDを条件として全カレンダ取得
			ct = QueryUtil.getAllCalInfo_OR(ownerRoleId);
		}
		return new ArrayList<>(ct);
	}

	/**
	 * カレンダID一覧を取得します。<BR>
	 * 
	 * @return カレンダID一覧
	 */
	public ArrayList<String> getCalendarIdList() {
		ArrayList<String> list = new ArrayList<String>();

		//全カレンダを取得
		List<CalendarInfo> ct = QueryUtil.getAllCalInfo();
		for (CalendarInfo cal : ct) {
			list.add(cal.getCalendarId());
		}
		return list;
	}

	/**
	 * 指定されたカレンダIDをもとに
	 * 月カレンダビューに表示する情報を取得します
	 * @param id
	 * @param year
	 * @param month
	 * @return
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public ArrayList<Integer> getCalendarMonth(String id, Integer year, Integer month) throws CalendarNotFound, InvalidRole {
		return getCalendarMonth(getCalendarFull(id), year, month);
	}

	/**
	 * 月カレンダビューに表示する情報を取得します
	 * @param info
	 * @param year
	 * @param month
	 * @return
	 */
	public ArrayList<Integer> getCalendarMonth(CalendarInfo info, Integer year, Integer month) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		ArrayList<CalendarDetailInfo>list24 = new ArrayList<CalendarDetailInfo>();
		for (CalendarDetailInfo d : info.getCalendarDetailList()) {
			list24.addAll(CalendarUtil.getDetail24(d));
		}

		long validFrom = info.getValidTimeFrom();
		long validTo = info.getValidTimeTo();

		Calendar cal = HinemosTime.getCalendarInstance();
		cal.set(year, month - 1, 1);
		int lastDate = cal.getActualMaximum(Calendar.DATE) + 1;
		m_log.debug("maxDate=" + year + "/" + month + "/" + lastDate);
		for (int i = 1; i < lastDate; i ++) {
			Calendar startCalendar = HinemosTime.getCalendarInstance();
			startCalendar.clear();
			startCalendar.set(year, month - 1, i, 0, 0, 0);
			long dayStartTime = startCalendar.getTimeInMillis();
			Calendar endCalendar = HinemosTime.getCalendarInstance();
			endCalendar.clear();
			endCalendar.set(year, month - 1, i + 1, 0, 0, 0);
			long dayEndTime = endCalendar.getTimeInMillis();
			m_log.debug("i=" + i + " ==== start=" + new Date(dayStartTime) + ",end=" + new Date(dayEndTime));
			
			
			// 1日の時間内に非有効期間がある場合に立てるフラグ
			// （このフラグがtrueの場合、最後の判定の際に強制的に○から△に変更する）
			boolean isContainInvalidPeriod = false;
			// 有効期限を加味したその日の期間を計算する
			long dayValidStart;
			if (dayStartTime < validFrom) {
				dayValidStart = validFrom;
				isContainInvalidPeriod = true;
			} else {
				dayValidStart = dayStartTime;
			}
			long dayValidEnd;
			if (validTo < dayEndTime) {
				dayValidEnd = validTo;
				isContainInvalidPeriod = true;
			} else {
				dayValidEnd = dayEndTime;
			}
			// 日の最後・有効期間の最後は有効期間に含まれないため、デクリメントする
			dayValidEnd--;
			
			// その日が有効期限内に入っていない場合、無条件に×とする
			if (dayValidStart > dayValidEnd) {
				ret.add(2);
				continue;
			}
			
			/**
			 * 境界時刻をリストアップする。境界時刻とは以下のとおり
			 * ・その日の最初と最後（但しカレンダの有効期限が短ければカレンダの有効期限範囲）
			 * ・各CalendarDetailInfoのFromとTo（但し上記の有効期限内のものに限る）
			 */
			Set<Long> borderTimeSet = new HashSet<Long>();
			borderTimeSet.add(dayValidStart);
			borderTimeSet.add(dayValidEnd);
			// detail
			for (CalendarDetailInfo detail : list24) {
				long detailStart = dayStartTime + detail.getTimeFrom() + TIMEZONE;
				if (dayValidStart < detailStart && detailStart < dayValidEnd) {
					borderTimeSet.add(detailStart);
				}
				
				long detailEnd = dayStartTime + detail.getTimeTo() + TIMEZONE;
				if (dayValidStart < detailEnd && detailEnd < dayValidEnd) {
					borderTimeSet.add(detailEnd);
				}
			}
			
			/**
			 * 全境界時刻について、
			 * ・○[0]・・・すべてが有効（つまり全境界時刻がOK）
			 * ・×[2]・・・すべてが無効（つまり全境界時刻がNG）
			 * ・△[1]・・・一部がOKで一部がNG
			 * をチェックする
			 */
			boolean isAllNG = true; // OKを見つけた時点でfalseに遷移
			boolean isAllOK = true; // NGを見つけた時点でfalseに遷移
			for (Long borderTime : borderTimeSet) {
				// この境界時刻が動作時刻か、非動作時刻かを検証する
				// カレンダ詳細設定から、この境界時刻時点で稼動か否かを調査する
				m_log.debug("date:" + new Date(borderTime));
				boolean retRun = CalendarUtil.isRun(info, new Date(borderTime));
				if (retRun) {
					isAllNG = false;
				} else {
					isAllOK = false;
				}
				// 全OK・全NGではなくなったら△に確定なので、残りの処理は行わない
				if (isAllNG == false && isAllOK == false) {
					break;
				}
			}
			
			if (isAllNG == true) {
				// ×：全部NG
				ret.add(2);
			} else {
				if (isAllOK == true) {
					if (isContainInvalidPeriod) {
						// △：一部OK・一部NG （有効期間内は全てOKだが、カレンダそのものの非有効範囲が被るため）
						ret.add(1);
					} else {
						// ○：全てOK
						ret.add(0);
					}
				} else {
					// △：一部OK・一部NG
					ret.add(1);
				}
			}
		}
		return ret;
	}
	/**
	 * カレンダ詳細定義 - 年、月、日が現在の時間が等しいか調べる
	 * 時間、分、秒は見ない。
	 * CalendarWeekViewで利用する。
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */

	public ArrayList<CalendarDetailInfo> getCalendarWeek(String id, Integer year, Integer month, Integer day) throws CalendarNotFound, InvalidRole {

		CalendarInfo info = getCalendarFull(id);
		return getCalendarWeek(info, year, month, day);
	}

	public ArrayList<CalendarDetailInfo> getCalendarWeek(CalendarInfo info, Integer year, Integer month, Integer day) throws CalendarNotFound {
		m_log.trace("calendarId:" + info.getCalendarId() + ", year:" + year + ", month:" + month + ", day:" + day);
		long validFrom = info.getValidTimeFrom();
		long validTo = info.getValidTimeTo();
		ArrayList<CalendarDetailInfo> ret = new ArrayList<CalendarDetailInfo>();
		Calendar startCalendar = HinemosTime.getCalendarInstance();
		startCalendar.clear();
		startCalendar.set(year, month - 1, day, 0, 0, 0);
		long startTime = startCalendar.getTimeInMillis();
		Calendar endCalendar = HinemosTime.getCalendarInstance();
		endCalendar.clear();
		endCalendar.set(year, month - 1, day + 1, 0, 0, 0);
		long endTime = endCalendar.getTimeInMillis();

		if (startTime <=  validFrom && endTime <= validFrom) {
			return ret;
		}
		if (validTo <= startTime && validTo <= endTime) {
			return ret;
		}
		if (startTime < validFrom && validFrom < endTime) {
			CalendarDetailInfo detail = new CalendarDetailInfo();
			detail.setTimeFrom(0 - TIMEZONE);
			detail.setTimeTo(validFrom - startTime - TIMEZONE);
			detail.setOperateFlg(false);
			detail.setOrderNo(-2);// ソート用に入れる
			m_log.trace("start<validfrom && validfrom<endttime add");
			ret.add(detail);
		}
		if (startTime < validTo && validTo < endTime) {
			CalendarDetailInfo detail = new CalendarDetailInfo();
			detail.setTimeFrom(validTo - startTime - TIMEZONE);
			detail.setTimeTo(HOUR24 - TIMEZONE);
			detail.setOperateFlg(false);
			detail.setOrderNo(-1);// ソート用に入れる
			m_log.trace("start<validto && validto<endttime add");
			ret.add(detail);
		}

		// clientから指定された日に関係するCalendarDetailListを返すため
		// チェックすべき日付の洗い出しをする
		startCalendar.clear();
		startCalendar.set(year, month - 1, day, 0, 0, 0);
		ArrayList<Date> checkDateList = new ArrayList<>();
		int onesec = 1*1000;
		Long startLong = startCalendar.getTime().getTime();
		ArrayList<CalendarDetailInfo> substituteList = new ArrayList<>();
		
		for (CalendarDetailInfo detailInfo : info.getCalendarDetailList()) {
			int timezone = HinemosTime.getTimeZoneOffset();
			// start_time
			checkDateList.add(new Date(startLong + detailInfo.getTimeFrom() + timezone));
			checkDateList.add(new Date(startLong + detailInfo.getTimeFrom() - onesec + timezone));// 1secondを引く
			checkDateList.add(new Date(startLong + detailInfo.getTimeFrom() + onesec + timezone));// 1secondを足す
			// end_time
			checkDateList.add(new Date(startLong + detailInfo.getTimeTo() + timezone));
			checkDateList.add(new Date(startLong + detailInfo.getTimeTo() - onesec + timezone));
			checkDateList.add(new Date(startLong + detailInfo.getTimeTo() + onesec + timezone));
			if (detailInfo.isSubstituteFlg()) {
				// 振り替え情報があるかチェックするためリストに入れる
				substituteList.add(detailInfo);
			}
		}
		
		// getDetail24した後の日時で洗い出しをする
		ArrayList<CalendarDetailInfo> detailList = new ArrayList<>();
		for (CalendarDetailInfo detail : info.getCalendarDetailList()) {
			detailList.addAll(CalendarUtil.getDetail24(detail));
		}
		for (CalendarDetailInfo detailInfo : detailList) {
			int timezone = HinemosTime.getTimeZoneOffset();
			// start_time
			checkDateList.add(new Date(startLong + detailInfo.getTimeFrom() + timezone));
			checkDateList.add(new Date(startLong + detailInfo.getTimeFrom() - onesec + timezone));// 1secondを引く
			checkDateList.add(new Date(startLong + detailInfo.getTimeFrom() + onesec + timezone));// 1secondを足す
			// end_time
			checkDateList.add(new Date(startLong + detailInfo.getTimeTo() + timezone));
			checkDateList.add(new Date(startLong + detailInfo.getTimeTo() - onesec + timezone));
			checkDateList.add(new Date(startLong + detailInfo.getTimeTo() + onesec + timezone));
		}
		
		// すでにリストに追加済みの情報に、振り替え日時を意識した日時をリストに入れる
		ArrayList<Date> checkDateListSub = new ArrayList<>();
		for (Date checkDate : checkDateList) {
			for (CalendarDetailInfo detailInfo : substituteList) {
				Long checkDateLong = checkDate.getTime();
				checkDateListSub.add(new Date(checkDateLong - (CalendarUtil.parseDate(detailInfo.getSubstituteTime()) + HinemosTime.getTimeZoneOffset())));
			}
		}
		checkDateList.addAll(checkDateListSub);
		
		// カレンダー情報のvalid_time_fromとvalid_time_to
		checkDateList.add(new Date(info.getValidTimeFrom()));
		checkDateList.add(new Date(info.getValidTimeTo()));
		// list内の重複をなくす
		checkDateList = new ArrayList<>(new HashSet<>(checkDateList));
		// list内をソートする
		Collections.sort(checkDateList);
		m_log.trace("checkDateList.size:" + checkDateList.size());
		
		ArrayList<CalendarDetailInfo> detailSubsList = new ArrayList<>();
		for (Date targetDate : checkDateList) {
			Calendar targetCal = Calendar.getInstance();
			targetCal.setTime(targetDate);
			// チェック対象日時が大本の日時と違う場合はチェックを実施しない
			if (startCalendar.get(Calendar.YEAR) != targetCal.get(Calendar.YEAR)
					|| startCalendar.get(Calendar.MONTH) != targetCal.get(Calendar.MONTH)
					|| startCalendar.get(Calendar.DATE) != targetCal.get(Calendar.DATE)) {
				continue;
			}
			m_log.trace("startCalendar:" + targetDate);
			ArrayList<CalendarDetailInfo> detailList2 = new ArrayList<>();
			Date substituteDate = new Date(targetDate.getTime());
			
			// detailList2には、関連するCalendarDetailInfoが入る
			Object[] retObjArr = CalendarUtil.getCalendarRunDetailInfo(info, targetDate, detailList2);
			boolean isrun = (Boolean)retObjArr[0];
			// 戻り値が2つの場合は、振り替え実施
			if (retObjArr.length == 2) {
				substituteDate.setTime(((Date)retObjArr[1]).getTime());
			}
			m_log.trace("id:"+ info.getCalendarId() + ", startCalendar:" + targetDate + ", detailList2.size:" 
			+ detailList2.size() + ", isrun:" + isrun + ", substituteDate:" + substituteDate.toString());

			// 関連するCalendarDetailInfoが日跨ぎをしているかもしれないので、getDetail24のリストで対象日付のリストを取得する
			for (CalendarDetailInfo detailInfo : detailList2) {
				ArrayList<CalendarDetailInfo> calendarDetailList = CalendarUtil.getDetail24(detailInfo);
				for (CalendarDetailInfo detail24 : calendarDetailList) {
					if (CalendarUtil.isRunByDetailDateTime(detail24, substituteDate) && !detailSubsList.contains(detail24)) {
						detailSubsList.add(detail24);
						m_log.trace("add to ret. orderNo:" + detail24.getOrderNo() + ", description:" + detail24.getDescription() 
						+ ", isOperation:" + detail24.isOperateFlg() + ", from:" + detail24.getTimeFrom() + ", to:" + detail24.getTimeTo());
					}
				}
			}
		}
		ret.addAll(detailSubsList);
		Collections.sort(ret, new Comparator<CalendarDetailInfo>() {
			public int compare(CalendarDetailInfo obj0, CalendarDetailInfo obj1) {
				int order1 = obj0.getOrderNo();
				int order2 = obj1.getOrderNo();
				int ret = order1 - order2;
				return ret;
			}
		});
		
		if (m_log.isDebugEnabled()) {
			for (CalendarDetailInfo detail : ret) {
				m_log.debug("detail=" + detail);
			}
		}
		m_log.trace("ret.size:" + ret.size());
		return ret;
	}

	/**
	 * カレンダ[カレンダパターン]情報を取得します。
	 * 
	 * @param id
	 * @return カレンダ[カレンダパターン]情報
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public CalendarPatternInfo getCalendarPattern(String id) throws CalendarNotFound, InvalidRole {
		CalendarPatternInfo ret = null;
		if(id != null && !"".equals(id)){
			ret = CalendarPatternCache.getCalendarPatternInfo(id);
		} else {
			throw new CalendarNotFound("id is null");
		}
		// 年月日で昇順ソート
		if (ret.getYmd() != null) {
			Collections.sort(ret.getYmd(), new Comparator<YMD>(){
				@Override
				public int compare(YMD y1, YMD y2) {
					Calendar ymd1 = Calendar.getInstance();
					ymd1.set(y1.getYear(), y1.getMonth() - 1, y1.getDay());
					Calendar ymd2 = Calendar.getInstance();
					ymd2.set(y2.getYear(), y2.getMonth() - 1, y2.getDay());
					return ymd1.getTime().compareTo(ymd2.getTime());
				}
			});
		}
		return ret;
	}

	/**
	 * カレンダ[カレンダパターン]情報一覧を取得します。
	 * 
	 * @param ownerRoleId オーナーロールID
	 * @return カレンダ[カレンダパターン]情報のリスト
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public ArrayList<CalendarPatternInfo> getCalendarPatternList(String ownerRoleId) throws CalendarNotFound, InvalidRole {
		ArrayList<CalendarPatternInfo> list = new ArrayList<CalendarPatternInfo>();
		//全カレンダを取得
		ArrayList<String> patternIdList = getCalendarPatternIdList(ownerRoleId);
		for (String id : patternIdList) {
			CalendarPatternInfo info = CalendarPatternCache.getCalendarPatternInfo(id);
			// 年月日で昇順ソート
			if (info.getYmd() != null) {
				Collections.sort(info.getYmd(), new Comparator<YMD>(){
					@Override
					public int compare(YMD y1, YMD y2) {
						Calendar ymd1 = Calendar.getInstance();
						ymd1.set(y1.getYear(), y1.getMonth() - 1, y1.getDay());
						Calendar ymd2 = Calendar.getInstance();
						ymd2.set(y2.getYear(), y2.getMonth() - 1, y2.getDay());
						return ymd1.getTime().compareTo(ymd2.getTime());
					}
				});
			}
			list.add(info);
		}
		/*
		 * カレンダパターンIDで昇順ソート
		 */
		Collections.sort(list, new Comparator<CalendarPatternInfo>() {
			@Override
			public int compare(CalendarPatternInfo o1, CalendarPatternInfo o2) {
				return o1.getCalPatternId().compareTo(o2.getCalPatternId());
			}
		});
		return list;
	}

	/**
	 * カレンダパターンID一覧を取得します。<BR>
	 * 
	 * @param ownerRoleId オーナーロールID
	 * @return カレンダパターンのID一覧
	 */
	public ArrayList<String> getCalendarPatternIdList(String ownerRoleId) {
		ArrayList<String> list = new ArrayList<String>();
		//全カレンダパターンを取得
		List<CalendarPatternInfo> entityList = QueryUtil.getAllCalPatternInfo();
		if (ownerRoleId == null || ownerRoleId.isEmpty()) {
			entityList = QueryUtil.getAllCalPatternInfo();
		} else {
			entityList = QueryUtil.getAllCalPatternInfo_OR(ownerRoleId);
		}
		for (CalendarPatternInfo entity : entityList) {
			list.add(entity.getCalPatternId());
		}
		//ソート処理
		Collections.sort(list);
		return list;
	}

	/**
	 * 実行可能かをチェックします。<BR>
	 * 
	 * 指定カレンダにて、指定した日時が実行可能かチェックし、Bool値を返します。
	 * 
	 * @param id
	 * @param checkTimestamp
	 * @return 指定した日時が実行可能か
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public Boolean isRun(String id, Long checkTimestamp) throws CalendarNotFound, InvalidRole {
		CalendarInfo info = null;
		Date date = new Date(checkTimestamp);
		if (id == null) {
			return true;
		}
		info = getCalendarFull(id);

		return CalendarUtil.isRun(info, date);
	}

	/**
	 * テスト用
	 * @param args
	 */
	public static void main(String args[]) {
		monthTest();
	}
	/**
	 * 月カレンダビュー表示テスト
	 */
	public static void monthTest() {
		CalendarInfo info = new CalendarInfo();
		info.setValidTimeFrom(0l);
		info.setValidTimeTo(Long.MAX_VALUE);

		ArrayList<CalendarDetailInfo> detailList = new ArrayList<CalendarDetailInfo>();
		CalendarDetailInfo detail = null;


		detail = new CalendarDetailInfo();
		detail.setYear(2012);
		detail.setMonth(0); // 全ての月は0
		detail.setDayType(0);//毎日を選択
		detail.setDayType(1);//曜日を選択
		detail.setDayOfWeekInMonth(0);//第ｘ週、0は毎週
		detail.setDayOfWeek(1);//曜日、1は日曜日
		//		detail.setTimeFrom(0*3600*1000l - TIMEZONE);
		detail.setTimeFrom(1*3600*1000l - TIMEZONE);
		detail.setTimeTo(23*3600*1000l - TIMEZONE);
		//		detail.setTimeTo(24*3600*1000l - TIMEZONE);
		//		detail.setTimeTo(25*3600*1000l - TIMEZONE);
		detail.setOperateFlg(true);
		detailList.add(detail);

		info.setCalendarDetailList(detailList);

		SelectCalendar selectCalendar = new SelectCalendar();
		ArrayList<Integer> list = selectCalendar.getCalendarMonth(info, 2012, 2);
		int j = 0;
		StringBuilder str = new StringBuilder();
		for (Integer i : list) {
			if (j % 7 == 0) {
				str.append("\n");
			}
			str.append(i).append(" ");
			j++;
		}
		m_log.trace("getCalendarMonthInfo=" + str);
	}
}
