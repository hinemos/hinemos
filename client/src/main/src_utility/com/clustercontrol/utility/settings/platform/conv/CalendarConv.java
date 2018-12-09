/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.xml.CalendarDetailInfo;
import com.clustercontrol.utility.settings.platform.xml.CalendarInfo;
import com.clustercontrol.utility.settings.platform.xml.CalendarPatternDetailInfo;
import com.clustercontrol.utility.settings.platform.xml.CalendarPatternInfo;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.DateUtil;
import com.clustercontrol.utility.util.TimeTo48hConverter;

/**
 * カレンダー情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class CalendarConv {
	
	static private final String schemaType="H";
	static private final String schemaVersion="1";
	static private final String schemaRevision="1" ;

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static Log log = LogFactory.getLog(CalendarConv.class);

	/**
	 * XMLとツールの対応バージョンをチェック */
	static public int checkSchemaVersion(String type, String version ,String revision){

		return BaseConv.checkSchemaVersion(
				schemaType,schemaVersion,schemaRevision,
				type,version,revision);
	}

	/**
	 * スキーマのバージョンを返します。
	 * @return
	 */
	static public com.clustercontrol.utility.settings.platform.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.platform.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	/**
	 * カレンダ定義に関して、XML BeanからHinemos Beanへ変換する。
	 * 
	 * @param info
	 *            カレンダ定義 XML Bean
	 * @return カレンダ定義 Hinemos Bean
	 * @throws ParseException
	 */
	public static com.clustercontrol.ws.calendar.CalendarInfo getCalendarInfoDto(CalendarInfo info) throws ParseException {
		
		com.clustercontrol.ws.calendar.CalendarInfo ret = new com.clustercontrol.ws.calendar.CalendarInfo();
		

		// 登録日時、更新日時に利用する日時（実行日時とする）
		long now = new Date().getTime();

		if(info.getCalendarId() != null){
			ret.setCalendarId(info.getCalendarId());
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid") + "(CalendarId) : " + info.toString());
			return null;
		}
		
		if(info.getCalendarName() != null){
			ret.setCalendarName(info.getCalendarName());
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid") + "(CalendarName) : " + info.getCalendarId());
			return null;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(TimezoneUtil.getTimeZone());
		Date date = sdf.parse(info.getValidTimeFrom());
		ret.setValidTimeFrom(date.getTime());
		date = sdf.parse(info.getValidTimeTo());
		ret.setValidTimeTo(date.getTime());
		
		if(info.getDescription() != null){
			ret.setDescription(info.getDescription());
		}
		
		CalendarDetailInfo[] details = info.getCalendarDetailInfo();
		Arrays.sort(details, new Comparator<CalendarDetailInfo>() {
			@Override
			public int compare(CalendarDetailInfo o1, CalendarDetailInfo o2) {
				return o1.getOrderNo() - o2.getOrderNo();
			}
		});
		for (CalendarDetailInfo detail : details) {
			com.clustercontrol.ws.calendar.CalendarDetailInfo retDetail =
					getCalendarDetailInfoDto(info.getCalendarId(), detail);
			ret.getCalendarDetailList().add(retDetail);
		}
		
		ret.setOwnerRoleId(info.getOwnerRoleId());
		
		ret.setRegDate(now);
		ret.setRegUser(Config.getConfig("Login.USER"));
		ret.setUpdateDate(now);
		ret.setUpdateUser(Config.getConfig("Login.USER"));

		return ret;
	}

	/**
	 * カレンダ詳細定義に関して、XML BeanからHinemos Beanへ変換する。
	 * 
	 * @param calenderId カレンダID
	 * @param info カレンダ詳細定義 XML Bean
	 * @return カレンダ詳細定義 Hinemos Bean
	 * @throws ParseException
	 */
	private static com.clustercontrol.ws.calendar.CalendarDetailInfo getCalendarDetailInfoDto(String calenderId, CalendarDetailInfo info) throws ParseException {
		com.clustercontrol.ws.calendar.CalendarDetailInfo ret = null;

		ret = new com.clustercontrol.ws.calendar.CalendarDetailInfo();

		ret.setDescription(info.getDescription());
		ret.setYear(info.getYearNo());
		ret.setMonth(info.getMonthNo());

		ret.setDayType(info.getDayType());

		ret.setDayOfWeekInMonth(info.getWeekXth());
		ret.setDayOfWeek(info.getWeekNo());

		ret.setDate(info.getDayNo());
		
		if (info.getCalendarPatternId() != null && !info.getCalendarPatternId().isEmpty()) {
			ret.setCalPatternId(info.getCalendarPatternId());
		}
		else {
			ret.setCalPatternId(null);
		}

		ret.setAfterday(info.getAfterDay());
		
		ret.setTimeFrom(DateUtil.convTimeString2Epoch(info.getStartTime()));
		ret.setTimeTo(DateUtil.convTimeString2Epoch(info.getEndTime()));
		
		ret.setOperateFlg(info.getExecuteFlg());

		ret.setSubstituteFlg(info.getSubstituteFlg());
		ret.setSubstituteLimit(info.getSubstituteLimit());
		ret.setSubstituteTime(info.getSubstituteTime());

		return ret;
	}
	
	/**
	 * カレンダ定義に関して、Hinemos BeanからXML Beanへ変換する。
	 * 
	 * @param calendar
	 *            カレンダ定義 Hinemos Bean
	 * @return
	 * @throws ParseException
	 * @throws IndexOutOfBoundsException
	 */
	public static CalendarInfo getCalendarInfo(com.clustercontrol.ws.calendar.CalendarInfo calendar) throws IndexOutOfBoundsException, ParseException {
		CalendarInfo ret = new CalendarInfo();

		ret.setCalendarId(calendar.getCalendarId());
		ret.setCalendarName(calendar.getCalendarName());
	
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(TimezoneUtil.getTimeZone());
		ret.setValidTimeFrom( sdf.format(calendar.getValidTimeFrom()));
		ret.setValidTimeTo(sdf.format(calendar.getValidTimeTo()));

		ret.setDescription(calendar.getDescription());
		ret.setOwnerRoleId(calendar.getOwnerRoleId());

		int oderNo = 1;
		for (com.clustercontrol.ws.calendar.CalendarDetailInfo detail : calendar.getCalendarDetailList()) {
			CalendarDetailInfo retDetail = getCalendarDetailInfo(detail);
			retDetail.setOrderNo(oderNo);
			ret.addCalendarDetailInfo(retDetail);
			oderNo++;
		}

		return ret;
	}

	/**
	 * カレンダ詳細定義に関して、Hinemos BeanからXML Beanへ変換する。
	 * 
	 * @param info
	 *            カレンダ詳細定義 Hinemos Bean
	 * @return カレンダ詳細定義 XML Bean
	 * @throws ParseException
	 */
	private static CalendarDetailInfo getCalendarDetailInfo(com.clustercontrol.ws.calendar.CalendarDetailInfo info) throws ParseException {
		CalendarDetailInfo ret = new CalendarDetailInfo();

		ret.setDescription(info.getDescription());
		ret.setYearNo(info.getYear());
		ret.setMonthNo(info.getMonth());
		ret.setDayType(info.getDayType());
		if(info.getDayType() == 1){
			ret.setWeekXth(info.getDayOfWeekInMonth());
			ret.setWeekNo(info.getDayOfWeek());
			ret.setAfterDay(info.getAfterday());
		}
		else if(info.getDayType() == 2){
			ret.setDayNo(info.getDate());
			ret.setAfterDay(info.getAfterday());
		}
		else if(info.getDayType() == 3){
			if (info.getCalPatternId() != null && !info.getCalPatternId().isEmpty()) {
				ret.setCalendarPatternId(info.getCalPatternId());
			}
			ret.setAfterDay(info.getAfterday());
		}
		ret.setStartTime(TimeTo48hConverter.dateTo48hms(info.getTimeFrom()));
		ret.setEndTime(TimeTo48hConverter.dateTo48hms(info.getTimeTo()));
		ret.setExecuteFlg(info.isOperateFlg());

		ret.setSubstituteFlg(info.isSubstituteFlg());
		ret.setSubstituteLimit(info.getSubstituteLimit());
		ret.setSubstituteTime(info.getSubstituteTime());

		return ret;
	}

	/**
	 * カレンダパターン定義に関して、XML BeanからHinemos Beanへ変換する。
	 * 
	 * @param info
	 *            カレンダパターン定義 XML Bean
	 * @return カレンダパターン定義 Hinemos Bean
	 * @throws ParseException
	 */
	public static com.clustercontrol.ws.calendar.CalendarPatternInfo getCalendarPatternInfoDto(CalendarPatternInfo info) throws ParseException {
		
		com.clustercontrol.ws.calendar.CalendarPatternInfo ret = new com.clustercontrol.ws.calendar.CalendarPatternInfo();
		
		try {
			// 登録日時、更新日時に利用する日時（実行日時とする）
			long now = new Date().getTime();

			if(info.getCalendarPatternId() != null){
				ret.setCalPatternId(info.getCalendarPatternId());
			}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") + "(CalendarPatternId) : " + info.toString());
				return null;
			}
			
			if(info.getCalendarPatternName() != null){
				ret.setCalPatternName(info.getCalendarPatternName());
			}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") + "(CalendarPatternName) : " + info.getCalendarPatternId());
				return null;
			}
			
			if(info.getOwnerRoleId() != null){
				ret.setOwnerRoleId(info.getOwnerRoleId());
			}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") + "(OwnerRoleId) : " + info.getCalendarPatternId());
				return null;
			}
					
			for (CalendarPatternDetailInfo detail : info.getCalendarPatternDetailInfo()) {
				com.clustercontrol.ws.calendar.Ymd retDetail = getCalendarPatternDetailInfoDto(info.getCalendarPatternId(), detail);
				ret.getYmd().add(retDetail);
			}
			
			ret.setRegDate(now);
			ret.setRegUser(Config.getConfig("Login.USER"));
			ret.setUpdateDate(now);
			ret.setUpdateUser(Config.getConfig("Login.USER"));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return ret;
	}

	/**
	 * カレンダパターン詳細定義に関して、XML BeanからHinemos Beanへ変換する。
	 * 
	 * @param calenderPatternId カレンダパターンID
	 * @param info カレンダパターン詳細定義 XML Bean
	 * @return カレンダパターン詳細定義 Hinemos Bean
	 * @throws ParseException
	 */
	private static com.clustercontrol.ws.calendar.Ymd getCalendarPatternDetailInfoDto(String calenderPatternId, CalendarPatternDetailInfo info) throws ParseException {
		com.clustercontrol.ws.calendar.Ymd ret = null;

		try {
			ret = new com.clustercontrol.ws.calendar.Ymd();

			ret.setYear(info.getYearNo());
			ret.setMonth(info.getMonthNo());
			ret.setDay(info.getDayNo());
		} catch (Exception e) {
			log.error(e);
		}

		return ret;
	}
	
	/**
	 * カレンダパターン定義に関して、Hinemos BeanからXML Beanへ変換する。
	 * 
	 * @param calendarPattern
	 *            カレンダパターン定義 Hinemos Bean
	 * @return
	 * @throws ParseException
	 * @throws IndexOutOfBoundsException
	 */
	public static CalendarPatternInfo getCalendarPatternInfo(com.clustercontrol.ws.calendar.CalendarPatternInfo calendarPattern) throws IndexOutOfBoundsException, ParseException {
		CalendarPatternInfo ret = new CalendarPatternInfo();

		ret.setCalendarPatternId(calendarPattern.getCalPatternId());
		ret.setCalendarPatternName(calendarPattern.getCalPatternName());
		ret.setOwnerRoleId(calendarPattern.getOwnerRoleId());

		for (com.clustercontrol.ws.calendar.Ymd ymd : calendarPattern.getYmd()) {
			CalendarPatternDetailInfo retDetail = getCalendarPatternDetailInfo(ymd);
			ret.addCalendarPatternDetailInfo(retDetail);
		}

		return ret;
	}
	
	/**
	 * カレンダパターン詳細定義に関して、Hinemos BeanからXML Beanへ変換する。
	 * 
	 * @param info
	 *            カレンダパターン詳細定義 Hinemos Bean
	 * @return カレンダパターン詳細定義 XML Bean
	 * @throws ParseException
	 */
	private static CalendarPatternDetailInfo getCalendarPatternDetailInfo(com.clustercontrol.ws.calendar.Ymd ymd) throws ParseException {
		CalendarPatternDetailInfo ret = new CalendarPatternDetailInfo();

		ret.setYearNo(ymd.getYear());
		ret.setMonthNo(ymd.getMonth());
		ret.setDayNo(ymd.getDay());

		return ret;
	}

}
