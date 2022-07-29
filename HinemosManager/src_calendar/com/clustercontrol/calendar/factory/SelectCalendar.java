/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.factory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

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
	private static final long HOUR = 60 * 60 * 1000;
	private static final long HOUR24 = 24 * HOUR;

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
	public Map<Integer, Integer> getCalendarMonth(String id, Integer year, Integer month) throws CalendarNotFound, InvalidRole {
		return getCalendarMonth(getCalendarFull(id), year, month);
	}

	/**
	 * 月カレンダビューに表示する情報を取得します
	 * @param info
	 * @param year
	 * @param month
	 * @return
	 */
	public Map<Integer, Integer> getCalendarMonth(CalendarInfo info, Integer year, Integer month) {
		Map<Integer, Integer> ret = new HashMap<>();
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
				ret.put(i, 2);
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
				ret.put(i, 2);
			} else {
				if (isAllOK == true) {
					if (isContainInvalidPeriod) {
						// △：一部OK・一部NG （有効期間内は全てOKだが、カレンダそのものの非有効範囲が被るため）
						ret.put(i, 1);
					} else {
						// ○：全てOK
						ret.put(i, 0);
					}
				} else {
					// △：一部OK・一部NG
					ret.put(i, 1);
				}
			}
		}
		return ret;
	}
	/**
	 * 週間予定で使用するための{@link CalendarDetailInfo}(※)を作成する。
	 *  <p>※ビューで使用する timeFrom, timeTo, operateFlg のみセットしている。
	 * @throws CalendarNotFound
	 * @throws InvalidRole
	 */
	public ArrayList<CalendarDetailInfo> getCalendarWeek(String id, Integer year, Integer month, Integer day) throws CalendarNotFound, InvalidRole {
		CalendarInfo info = getCalendarFull(id);
		return getCalendarWeek(info, year, month, day);
	}

	public ArrayList<CalendarDetailInfo> getCalendarWeek(CalendarInfo info, Integer year, Integer month, Integer day) throws CalendarNotFound {

		ArrayList<CalendarDetailInfo> ret = new ArrayList<>();
		
		// 稼働/非稼働の切り替わりは、カレンダ詳細(&カレンダ有効期限)の開始・終了時刻に
		// 限定される。
		// 例えば、カレンダに含まれる全てのカレンダ詳細の開始・終了時刻が"10:00～15:00"である
		// 場合(そして振り替え間隔が24の倍数である場合)、稼働/非稼働は必ず、10:00または
		// 15:00のどちらで切り替わる。
		// よって、開始・終了時刻を全てリストアップし、調査日の各時刻において判定を行い、
		// 稼働となる時間帯を求めればよい。
		// 調査日とは関わりのない時刻(例えば調査日が月曜の場合の、"振り替えなし毎週火曜日"の
		// カレンダ詳細の開始・終了時刻など)を判定から除外することで計算量を減らせるが、
		// 除外するかどうかの判断で逆に計算量が膨らみそうなので、未実装である。

		// メソッド内で使い回すカレンダ (Hinemosタイムゾーン)
		Calendar cal = HinemosTime.getCalendarInstance();
		cal.clear();

		// 調査日の始まりと終りの時刻を求める
		cal.set(year, month - 1, day, 0, 0, 0);
		long beginningOfDay = cal.getTimeInMillis();

		cal.set(year, month - 1, day + 1, 0, 0, 0);
		long endOfDay = cal.getTimeInMillis();

		// 調査日がカレンダの有効期間外の場合を刈り取る
		// (別にやらなくても問題ないが、少しでも効率化するため。)
		if (endOfDay <= info.getValidTimeFrom() || info.getValidTimeTo() <= beginningOfDay) {
			return ret;
		}

		// 判定時刻のリストアップ (ソート済み、重複なし)
		SortedSet<Date> checkTimes = new TreeSet<>();

		Consumer<Long> addCheckTime = new Consumer<Long>() {
			@Override
			public void accept(Long time) {
				// 指定された日時について、時刻(hh:mm:ss)は変えずに
				// 日付(yyyy-MM-dd)を調査日にすげ替えて、リストへ追加する
				cal.setTimeInMillis(time);
				cal.set(year, month - 1, day);
				cal.set(Calendar.MILLISECOND, 0);  // もしミリ秒が入っていたら重複判断が狂うので潰す
				checkTimes.add(cal.getTime());
			}
		};

		/// 調査日の始まり(終わりは判定不要)
		addCheckTime.accept(beginningOfDay);

		/// 有効期間  開始・終了時刻
		addCheckTime.accept(info.getValidTimeFrom());
		addCheckTime.accept(info.getValidTimeTo());

		/// カレンダ詳細  開始・終了時刻
		for (CalendarDetailInfo detail : info.getCalendarDetailList()) {

			// 振り替えが指定されている場合は、時刻をずらしながら上限回数分ループして追加する
			int substLimit = 1;
			long substDelta = 0;
			if (detail.isSubstituteFlg()) {
				substLimit = detail.getSubstituteLimit();
				substDelta = detail.getSubstituteTime() * HOUR;
			}
		
			for (int substCount = 0; substCount < substLimit; ++substCount) {
				for (long t : new long[] { detail.getTimeFrom(), detail.getTimeTo() }) {
					t += TIMEZONE;  // 日跨ぎ判定のため、0Lが"00:00:00"を指すUTCへ変換
					t += substDelta * substCount;  // 振り替え補正
					if (t < 0) {
						t = HOUR24 - (-t) % HOUR24;
					} else if (t > HOUR24) {
						t %= HOUR24;
					}
					addCheckTime.accept(t - TIMEZONE);      // TZを戻す
				}
			}
		}

		// 時刻ごとの稼働判定と、時間帯オブジェクト(CalendarDetailInfo)の生成
		List<CalendarDetailInfo> bands = new ArrayList<>();
		boolean prevResult = false;
		CalendarDetailInfo band = null;
		long timeFromToOffset = TIMEZONE + beginningOfDay;  // checkTime(Date型)のgetTimeはUTC基準なのでTIMEZONEを引く

		for (Date checkTime : checkTimes) {
			boolean checkResult = CalendarUtil.isRun(info, checkTime);
			if (m_log.isTraceEnabled()) {
				m_log.trace(checkTime + ", " + checkResult);
			}

			if (band == null) {
				// ループ初回(調査日の00:00:00)の場合は、最初の時間帯オブジェクトを用意するだけ
				band = new CalendarDetailInfo();
				band.setTimeFrom(checkTime.getTime() - timeFromToOffset);
				band.setOperateFlg(checkResult);
			} else {
				if (checkResult != prevResult) {
					// 稼働判定の切り替わりを検出したら、時間帯オブジェクトをリストへ追加
					band.setTimeTo(checkTime.getTime() - timeFromToOffset);
					bands.add(band);
					// 次の時間帯オブジェクト
					band = new CalendarDetailInfo();
					band.setTimeFrom(checkTime.getTime() - timeFromToOffset);
					band.setOperateFlg(checkResult);
				}
			}
			prevResult = checkResult;

		}

		// 最後の時間帯オブジェクトが宙に浮いているので、忘れずにリストへ追加する
		band.setTimeTo(endOfDay - timeFromToOffset);
		bands.add(band);

		// 稼働時間帯のみ返す
		for (CalendarDetailInfo b : bands) {
			if (b.isOperateFlg()) {
				ret.add(b);
			}
		}
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
		Map<Integer, Integer> map = selectCalendar.getCalendarMonth(info, 2012, 2);
		int j = 0;
		StringBuilder str = new StringBuilder();
		for (Integer i : map.values()) {
			if (j % 7 == 0) {
				str.append("\n");
			}
			str.append(i).append(" ");
			j++;
		}
		m_log.trace("getCalendarMonthInfo=" + str);
	}
}
