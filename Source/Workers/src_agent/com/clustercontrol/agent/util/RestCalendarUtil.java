/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import java.util.ArrayList;
import java.util.Date;

import org.openapitools.client.model.AgtCalendarDetailInfoResponse;
import org.openapitools.client.model.AgtCalendarInfoResponse;
import org.openapitools.client.model.AgtCalendarPatternInfoResponse;
import org.openapitools.client.model.AgtCalendarYmdResponse;

import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.calendar.model.YMD;
import com.clustercontrol.calendar.util.CalendarUtil;
import com.clustercontrol.util.HinemosTime;

public class RestCalendarUtil {

	/**
	 * {@link CalendarUtil#isRun(CalendarInfo, java.util.Date)} の REST API レスポンス向けアダプターメソッドです。
	 */
	public static boolean isRun(AgtCalendarInfoResponse info) {
		return CalendarUtil.isRun(convert(info), HinemosTime.getDateInstance());
	}

	public static boolean isRun(AgtCalendarInfoResponse info, Date date) {
		return CalendarUtil.isRun(convert(info), date);
	}

	protected static CalendarInfo convert(AgtCalendarInfoResponse wInfo) {
		if (wInfo == null) {
			return null;
		}

		CalendarInfo cInfo = new CalendarInfo();

		// cInfo.setDescription(wInfo.getDescription());
		cInfo.setCalendarId(wInfo.getCalendarId());
		// cInfo.setName(wInfo.getName());
		// cInfo.setRegDate(wInfo.getRegDate());
		// cInfo.setRegUser(wInfo.getRegUser());
		// cInfo.setUpdateDate(wInfo.getUpdateDate());
		// cInfo.setUpdateUser(wInfo.getUpdateUser());
		cInfo.setValidTimeFrom(wInfo.getValidTimeFrom());
		cInfo.setValidTimeTo(wInfo.getValidTimeTo());

		ArrayList<CalendarDetailInfo> list = new ArrayList<CalendarDetailInfo>();
		for (AgtCalendarDetailInfoResponse detail : wInfo.getCalendarDetailList()) {
			list.add(convertDetail(detail));
		}
		cInfo.setCalendarDetailList(list);
		return cInfo;
	}

	private static CalendarDetailInfo convertDetail(AgtCalendarDetailInfoResponse detail) {
		if (detail == null) {
			return null;
		}

		CalendarDetailInfo cInfo = new CalendarDetailInfo();
		cInfo.setAfterday(detail.getAfterday());
		cInfo.setDate(detail.getDate());
		cInfo.setDayOfWeek(detail.getDayOfWeek());
		cInfo.setDayOfWeekInMonth(detail.getDayOfWeekInMonth());
		cInfo.setDayType(detail.getDayType());
		// cInfo.setDescription(wInfo.getDescription());
		// cInfo.setEtcDays(wInfo.getEtcDays());
		//cInfo.setEtcInfo(ws2commonE(wInfo.getEtcInfo()));
		cInfo.setCalPatternId(detail.getCalPatternId());
		cInfo.setCalPatternInfo(convertPattern(detail.getCalPatternInfo()));
		cInfo.setMonth(detail.getMonth());
		cInfo.setOperateFlg(detail.getOperateFlg());
		cInfo.setTimeFrom(detail.getTimeFrom());
		cInfo.setTimeTo(detail.getTimeTo());
		cInfo.setYear(detail.getYear());
		cInfo.setSubstituteFlg(detail.getSubstituteFlg());
		cInfo.setSubstituteTime(detail.getSubstituteTime());
		cInfo.setSubstituteLimit(detail.getSubstituteLimit());

		return cInfo;
	}

	private static CalendarPatternInfo convertPattern(AgtCalendarPatternInfoResponse pattern) {
		if (pattern == null) {
			return null;
		}

		CalendarPatternInfo cInfo = new CalendarPatternInfo();
		if (pattern.getYmd() != null) {
			ArrayList<YMD> list = new ArrayList<YMD>();
			for (AgtCalendarYmdResponse ymd : pattern.getYmd()) {
				list.add(convertYmd(ymd));
			}
			cInfo.setYmd(list);
		}

		return cInfo;
	}

	private static YMD convertYmd(AgtCalendarYmdResponse wInfo) {
		YMD cInfo = new YMD();
		cInfo.setDay(wInfo.getDay());
		cInfo.setMonth(wInfo.getMonth());
		cInfo.setYear(wInfo.getYear());
		return cInfo;
	}
}
