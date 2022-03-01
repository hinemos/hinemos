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
import org.openapitools.client.model.CalendarDetailInfoResponse;
import org.openapitools.client.model.CalendarDetailInfoResponse.DayTypeEnum;
import org.openapitools.client.model.CalendarDetailInfoResponse.WeekNoEnum;
import org.openapitools.client.model.CalendarDetailInfoResponse.WeekXthEnum;
import org.openapitools.client.model.CalendarInfoResponse;
import org.openapitools.client.model.CalendarPatternInfoResponse;
import org.openapitools.client.model.YMDResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.xml.CalendarDetailInfo;
import com.clustercontrol.utility.settings.platform.xml.CalendarInfo;
import com.clustercontrol.utility.settings.platform.xml.CalendarPatternDetailInfo;
import com.clustercontrol.utility.settings.platform.xml.CalendarPatternInfo;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.OpenApiEnumConverter;

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
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static CalendarInfoResponse getCalendarInfoDto(CalendarInfo info) throws ParseException, InvalidSetting, HinemosUnknown {
		
		CalendarInfoResponse ret = new CalendarInfoResponse();
		

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
		
		ret.setValidTimeFrom(info.getValidTimeFrom());
		ret.setValidTimeTo(info.getValidTimeTo());
		
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
			CalendarDetailInfoResponse retDetail =
					getCalendarDetailInfoDto(info.getCalendarId(), detail);
			ret.getCalendarDetailList().add(retDetail);
		}
		
		ret.setOwnerRoleId(info.getOwnerRoleId());
		
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(TimezoneUtil.getTimeZone());
		String strDate = sdf.format(now);
		ret.setRegDate(strDate);
		ret.setRegUser(Config.getConfig("Login.USER"));
		ret.setUpdateDate(strDate);
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
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static CalendarDetailInfoResponse getCalendarDetailInfoDto(String calenderId, CalendarDetailInfo info) throws ParseException, InvalidSetting, HinemosUnknown {
		CalendarDetailInfoResponse ret = new CalendarDetailInfoResponse();
		
		ret.setDescription(info.getDescription());
		ret.setYearNo(info.getYearNo());
		ret.setMonthNo(info.getMonthNo());
		
		DayTypeEnum dayTypeEnum = OpenApiEnumConverter.integerToEnum(info.getDayType(), DayTypeEnum.class);
		ret.setDayType(dayTypeEnum);
		
		WeekXthEnum weekXthEnum = OpenApiEnumConverter.integerToEnum(info.getWeekXth(), WeekXthEnum.class); 
		ret.setWeekXth(weekXthEnum);
		
		WeekNoEnum weekNoEnum = null;
		if(info.getWeekNo() == 0){
			ret.setWeekNo(null);
		} else {
			weekNoEnum = OpenApiEnumConverter.integerToEnum(info.getWeekNo(), WeekNoEnum.class);
			ret.setWeekNo(weekNoEnum);
		}
		
		if(info.getDayNo() == 0){
			ret.setDayNo(null);
		} else {
			ret.setDayNo(info.getDayNo());
		}
		
		if (info.getCalendarPatternId() != null && !info.getCalendarPatternId().isEmpty() && info.getDayType() == 3) {
			ret.setCalPatternId(info.getCalendarPatternId());
		}
		else {
			ret.setCalPatternId(null);
		}
		
		ret.setAfterDay(info.getAfterDay());
		
		ret.setStartTime(info.getStartTime());
		ret.setEndTime(info.getEndTime());
		
		ret.setExecuteFlg(info.getExecuteFlg());
		
		ret.setSubstituteFlg(info.getSubstituteFlg());
		ret.setSubstituteLimit(info.getSubstituteLimit());
		ret.setSubstituteTime(info.getSubstituteTime());
		ret.setOrderNo(info.getOrderNo());

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
	public static CalendarInfo getCalendarInfo(CalendarInfoResponse calendar) throws IndexOutOfBoundsException, ParseException {
		CalendarInfo ret = new CalendarInfo();

		ret.setCalendarId(calendar.getCalendarId());
		ret.setCalendarName(calendar.getCalendarName());
	
		ret.setValidTimeFrom(calendar.getValidTimeFrom());
		ret.setValidTimeTo(calendar.getValidTimeTo());

		ret.setDescription(calendar.getDescription());
		ret.setOwnerRoleId(calendar.getOwnerRoleId());

		int oderNo = 1;
		for (CalendarDetailInfoResponse  detail : calendar.getCalendarDetailList()) {
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
	private static CalendarDetailInfo getCalendarDetailInfo(CalendarDetailInfoResponse info) throws ParseException {
		CalendarDetailInfo ret = new CalendarDetailInfo();

		ret.setDescription(info.getDescription());
		ret.setYearNo(info.getYearNo());
		ret.setMonthNo(info.getMonthNo());
		
		int DayTypeInt = OpenApiEnumConverter.enumToInteger(info.getDayType());
		ret.setDayType(DayTypeInt);
		if(DayTypeInt == 1){
			int weekXthInt = OpenApiEnumConverter.enumToInteger(info.getWeekXth());
			ret.setWeekXth(weekXthInt);
			
			int weekNoInt = OpenApiEnumConverter.enumToInteger(info.getWeekNo());
			ret.setWeekNo(weekNoInt);
			
			ret.setAfterDay(info.getAfterDay());
		}
		else if(DayTypeInt == 2){
			ret.setDayNo(info.getDayNo());
			ret.setAfterDay(info.getAfterDay());
		}
		else if(DayTypeInt == 3){
			if (info.getCalPatternId() != null && !info.getCalPatternId().isEmpty()) {
				ret.setCalendarPatternId(info.getCalPatternId());
			}
			ret.setAfterDay(info.getAfterDay());
		}
		ret.setStartTime(info.getStartTime());
		ret.setEndTime(info.getEndTime());
		ret.setExecuteFlg(info.getExecuteFlg());

		ret.setSubstituteFlg(info.getSubstituteFlg());
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
	public static CalendarPatternInfoResponse getCalendarPatternInfoDto(CalendarPatternInfo info) throws ParseException {
		
		CalendarPatternInfoResponse ret = new CalendarPatternInfoResponse();
		
		try {
			// 登録日時、更新日時に利用する日時（実行日時とする）
			long now = new Date().getTime();

			if(info.getCalendarPatternId() != null){
				ret.setCalendarPatternId(info.getCalendarPatternId());
			}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid") + "(CalendarPatternId) : " + info.toString());
				return null;
			}
			
			if(info.getCalendarPatternName() != null){
				ret.setCalendarPatternName(info.getCalendarPatternName());
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
				YMDResponse  retDetail = getCalendarPatternDetailInfoDto(info.getCalendarPatternId(), detail);
				ret.getCalPatternDetailInfoEntities().add(retDetail);
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			String strDate = sdf.format(now);
			ret.setRegDate(strDate);
			ret.setRegUser(Config.getConfig("Login.USER"));
			ret.setUpdateDate(strDate);
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
	private static YMDResponse getCalendarPatternDetailInfoDto(String calenderPatternId, CalendarPatternDetailInfo info) throws ParseException {
		YMDResponse ret = null;

		try {
			ret = new YMDResponse();

			ret.setYearNo(info.getYearNo());
			ret.setMonthNo(info.getMonthNo());
			ret.setDayNo(info.getDayNo());
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
	public static CalendarPatternInfo getCalendarPatternInfo(CalendarPatternInfoResponse calendarPattern) throws IndexOutOfBoundsException, ParseException {
		CalendarPatternInfo ret = new CalendarPatternInfo();

		ret.setCalendarPatternId(calendarPattern.getCalendarPatternId());
		ret.setCalendarPatternName(calendarPattern.getCalendarPatternName());
		ret.setOwnerRoleId(calendarPattern.getOwnerRoleId());

		for (YMDResponse ymd : calendarPattern.getCalPatternDetailInfoEntities()) {
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
	private static CalendarPatternDetailInfo getCalendarPatternDetailInfo(YMDResponse ymd) throws ParseException {
		CalendarPatternDetailInfo ret = new CalendarPatternDetailInfo();

		ret.setYearNo(ymd.getYearNo());
		ret.setMonthNo(ymd.getMonthNo());
		ret.setDayNo(ymd.getDayNo());

		return ret;
	}

}
