/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.system.conv;

import java.util.Calendar;
import java.util.Date;

import org.openapitools.client.model.AddMaintenanceRequest;
import org.openapitools.client.model.MaintenanceInfoResponse;
import org.openapitools.client.model.MaintenanceScheduleRequest;
import org.openapitools.client.model.MaintenanceScheduleResponse;

import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.maintenance.util.MaintenanceUtil;
import com.clustercontrol.utility.settings.maintenance.xml.MaintenanceInfo;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.OpenApiEnumConverter;

/**
 * メンテナンス設定情報をJavaBeanとXML(Bean)のbindingとのやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 * 
 * 
 * 
 */
public class MaintenanceConv {

	private static final String schemaType="H";
	private static final String schemaVersion="1";
	private static final String schemaRevision="1" ;
	
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
	static public com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	/**
	 * DTOのBeanからXMLのBeanに変換する。
	 * 
	 * @param info　メンテナンス情報DTOのBean
	 * @return
	 */
	public static MaintenanceInfo getMaintenanceInfo(
			MaintenanceInfoResponse info) {

		MaintenanceInfo ret = new MaintenanceInfo();

		//メンテナンス情報のセット(主部分)
		ret.setMaintenanceId(info.getMaintenanceId());
		if (info.getDescription() != null) {
			ret.setDescription(info.getDescription());
		} else {
			ret.setDescription("");
		}
		
		ret.setTypeId(info.getTypeId().getValue());
		ret.setDataRetentionPeriod(info.getDataRetentionPeriod());
		if (info.getCalendarId() != null) {
			ret.setCalendarId(info.getCalendarId());
		} else {
			ret.setCalendarId("");
		}
		ret.setOwnerRoleId(info.getOwnerRoleId());

		//通知部分のセット
		ret.setNotifyId( com.clustercontrol.utility.settings.platform.conv.CommonConv.notifyMaintennceDto2Xml(info.getNotifyId()));

		ret.setApplication(info.getApplication());
		ret.setValidFlg(info.getValidFlg());

		MaintenanceScheduleResponse schedule = info.getSchedule();
		if (schedule != null) {
			// カレンダオブジェクトに日付時刻を設定
			Calendar calendarWk = Calendar.getInstance();
			if (schedule.getMonth() != null && schedule.getMonth() > 0) {
				calendarWk.set(Calendar.MONTH, schedule.getMonth() - 1);
			}
			if (schedule.getDay() != null && schedule.getDay() > 0) {
				calendarWk.set(Calendar.DAY_OF_MONTH, schedule.getDay());
			}
			if (schedule.getHour() != null) {
				calendarWk.set(Calendar.HOUR_OF_DAY, schedule.getHour());
			}
			if (schedule.getMinute() != null) {
				calendarWk.set(Calendar.MINUTE, schedule.getMinute());
			}

			// スケジュール種別の設定
			int typeInt = OpenApiEnumConverter.enumToInteger(schedule.getType());
			ret.setType(typeInt);

			// スケジュール種別が曜日指定の場合、曜日を設定
			if (typeInt == ScheduleConstant.TYPE_WEEK && schedule.getWeek() != null) {
				ret.setDayOfWeek(schedule.getWeek());
			}

			// 日付時刻をカレンダオブジェクトより取得して設定
			if (schedule.getMonth() != null && schedule.getMonth() > 0) {
				ret.setMonth(calendarWk.get(Calendar.MONTH) + 1);
			}
			if (schedule.getDay() != null && schedule.getDay() > 0) {
				ret.setMday(calendarWk.get(Calendar.DAY_OF_MONTH));
			}
			if (schedule.getHour() != null) {
				ret.setHour(calendarWk.get(Calendar.HOUR_OF_DAY));
			}
			if (schedule.getMinute() != null) {
				ret.setMin(calendarWk.get(Calendar.MINUTE));
			}
			
		}

		return ret;
	}

	public static AddMaintenanceRequest getMaintenanceInfoData(
			MaintenanceInfo info) throws InvalidSetting, HinemosUnknown {
		AddMaintenanceRequest ret = new AddMaintenanceRequest();

		ret.setMaintenanceId(info.getMaintenanceId());
		if (info.getDescription() != null && !info.getDescription().isEmpty()) {
			ret.setDescription(info.getDescription());
		}
		ret.setTypeId(AddMaintenanceRequest.TypeIdEnum.fromValue(info.getTypeId()));
		// 2012/04/09 修正(カレンダIDが未設定の場合の対応)
		String calendarId = info.getCalendarId();
		ret.setCalendarId( calendarId.equals("") ? null : calendarId );
		ret.setOwnerRoleId(info.getOwnerRoleId());

		ret.setDataRetentionPeriod(info.getDataRetentionPeriod());

		//通知グループIDを生成します。
		String notifyGroupId = MaintenanceUtil.createNotifyGroupIdMaintenance(info.getMaintenanceId());
		
		com.clustercontrol.utility.settings.maintenance.xml.NotifyId[] notifies = info.getNotifyId();

		ret.getNotifyId().addAll(com.clustercontrol.utility.settings.platform.conv.CommonConv.notifyXml2Dto(notifyGroupId, notifies));
		
		ret.setApplication(info.getApplication());
		ret.setValidFlg(info.getValidFlg());

		ret.setSchedule(getSchedule(info));

		return ret;
	}
	
	/**
	 * ＸＭＬからHinemosのjava Bean(DTO)に変換する。
	 * 
	 * 
	 * @param info
	 * @return
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	private static MaintenanceScheduleRequest getSchedule(com.clustercontrol.utility.settings.maintenance.xml.MaintenanceInfo info) throws InvalidSetting, HinemosUnknown {

		MaintenanceScheduleRequest ret = new MaintenanceScheduleRequest();

		Calendar calendar = Calendar.getInstance();

		calendar.setTime(new Date(0));
		calendar.set(Calendar.YEAR, 0);

		MaintenanceScheduleRequest.TypeEnum typeEnum = OpenApiEnumConverter.integerToEnum(info.getType(),MaintenanceScheduleRequest.TypeEnum.class);
		ret.setType(typeEnum);

		if (info.hasMonth())
			if(info.getMonth() >0)
			{
				//表示上の月(1～12)をjava.util.Dateのmanth(0～11)に変換
				calendar.set(Calendar.MONTH, info.getMonth() - 1);
			}
		if (info.hasMday())
			if(info.getMday()>0)
			{
				calendar.set(Calendar.DAY_OF_MONTH, info.getMday());
			}
		if (info.hasDayOfWeek() && (!info.hasMday() || (info.hasMday() && info.getMday() == 0)))
			if(info.getDayOfWeek()>=0){
				calendar.set(Calendar.DAY_OF_WEEK, (int)info.getDayOfWeek());
				ret.setWeek((int)info.getDayOfWeek());
			}else{
				calendar.set(Calendar.DAY_OF_WEEK, 0);
				ret.setWeek((int)info.getDayOfWeek());
			}
		if (info.hasHour())
			if(info.getHour() >0){
				calendar.set(Calendar.HOUR_OF_DAY, info.getHour());
			}else{
				calendar.set(Calendar.HOUR_OF_DAY,0);
			}
		if(info.hasMin())
			if(info.getMin() >=0){
				calendar.set(Calendar.MINUTE, info.getMin());
			}else{
				calendar.set(Calendar.MINUTE, 0);
			}
		calendar.set(Calendar.SECOND, 0);

		if (info.hasMonth() && info.getMonth() > 0) {
			ret.setMonth(calendar.get(Calendar.MONTH) + 1);
		}
		if (info.hasMday() && info.getMday() > 0) {
			ret.setDay(calendar.get(Calendar.DAY_OF_MONTH));
		}
		if (info.hasHour() && info.getHour() >= 0) {
			ret.setHour(calendar.get(Calendar.HOUR_OF_DAY));
		}
		if (info.hasMin() && info.getMin() >= 0) {
			ret.setMinute(calendar.get(Calendar.MINUTE));
		} else {
			ret.setHour(0);
		}

		return ret;
	}
}
