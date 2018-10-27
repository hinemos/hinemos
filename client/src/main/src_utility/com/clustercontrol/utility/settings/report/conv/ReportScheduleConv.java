/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.report.conv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.clustercontrol.reporting.util.ReportingUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.report.xml.ReportingInfo;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.ws.common.Schedule;
import com.clustercontrol.ws.notify.NotifyRelationInfo;

public class ReportScheduleConv {
	
	static private final String schemaType="H";
	static private final String schemaVersion="1";
	static private final String schemaRevision="1" ;
	
	private static final int TYPE_EVERY_DAY = 0;
	private static final int TYPE_EVERY_WEEK = 1;
	private static final int TYPE_EVERY_MONTH = 2;
	
	/* ロガー */
	private static Logger log = Logger.getLogger(ReportScheduleConv.class);
	/**
	 * Versionなどの共通部分について、DTOからXMLのBeanに変換します。
	 * 
	 * @param ver　Version情報などのハッシュテーブル。
	 * @return
	 */
	public static com.clustercontrol.utility.settings.report.xml.Common
		versionReportDto2Xml(Hashtable<String,String> ver){
	
		com.clustercontrol.utility.settings.report.xml.Common com =
				new com.clustercontrol.utility.settings.report.xml.Common();

		com.setHinemosVersion(ver.get("hinemosVersion"));
		com.setToolVersion(ver.get("toolVersion"));
		com.setGenerator(ver.get("generator"));
		com.setAuthor(System.getProperty("user.name"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		com.setGenerateDate(dateFormat.format(new Date()));
		com.setRuntimeHost(ver.get("runtimeHost"));
		com.setConnectedManager(ver.get("connectedManager"));
		
		return com;
	}
	
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
	static public com.clustercontrol.utility.settings.report.xml.SchemaInfo getSchemaVersion(){
	
		com.clustercontrol.utility.settings.report.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.report.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	public static ReportingInfo getReporting(com.clustercontrol.ws.reporting.ReportingInfo reporting)
			throws IndexOutOfBoundsException, ParseException {
		
		ReportingInfo ret = new ReportingInfo();

		ret.setReportScheduleId(reporting.getReportScheduleId());
		ret.setDescription(reporting.getDescription());
		ret.setOwnerRoleId(reporting.getOwnerRoleId());
		ret.setFacilityId(reporting.getFacilityId());
		ret.setCalendarId(reporting.getCalendarId());
		ret.setOutputPeriodType(reporting.getOutputPeriodType());
		ret.setOutputPeriodBefore(reporting.getOutputPeriodBefore());
		ret.setOutputPeriodFor(reporting.getOutputPeriodFor());
		ret.setTemplateSetId(reporting.getTemplateSetId());
		ret.setReportTitle(reporting.getReportTitle());
		ret.setLogoValidFlg(reporting.isLogoValidFlg());
		ret.setLogoFilename(reporting.getLogoFilename());
		ret.setPageValidFlg(reporting.isPageValidFlg());
		ret.setOutputType(reporting.getOutputType());
		
		
		//・DTOのスケジュールタイプが「1」、日が未設定の場合 → XMLのスケジュールタイプは「毎日(0)」
		//・DTOのスケジュールタイプが「2」の場合 → XMLのスケジュールタイプは「毎週(1)」
		//・DTOのスケジュールタイプが「1」、日が設定済みの場合 → XMLのスケジュールタイプは「毎月(2)」
		if (reporting.getSchedule().getType() == com.clustercontrol.bean.ScheduleConstant.TYPE_WEEK){
			ret.setScheduleType(TYPE_EVERY_WEEK);
		} else {
			if (reporting.getSchedule().getDay() == null) {
				ret.setScheduleType(TYPE_EVERY_DAY);
			}else{
				ret.setScheduleType(TYPE_EVERY_MONTH);
			}
		}
		
		
		if (reporting.getSchedule().getDay() != null ){
			ret.setDay(reporting.getSchedule().getDay());
		}
		if (reporting.getSchedule().getWeek()!=null){
			ret.setWeek(reporting.getSchedule().getWeek());
		}
		
		ret.setHour(reporting.getSchedule().getHour());
		ret.setMinute(reporting.getSchedule().getMinute());
		ret.setNotifyGroupId(reporting.getNotifyGroupId());
		
		com.clustercontrol.utility.settings.report.xml.NotifyId[] notifyList =
				new com.clustercontrol.utility.settings.report.xml.NotifyId[reporting.getNotifyId().size()];
		int i=0;
		for(com.clustercontrol.ws.notify.NotifyRelationInfo info : reporting.getNotifyId()){
			notifyList[i] = new com.clustercontrol.utility.settings.report.xml.NotifyId();
			notifyList[i].setNotifyGroupId(info.getNotifyGroupId());
			notifyList[i].setNotifyId(info.getNotifyId());
			notifyList[i].setNotifyType(info.getNotifyType());
			i++;
		}
		ret.setNotifyId(notifyList);
		
		ret.setValidFlg(reporting.isValidFlg());
		
		return ret;
	}
	public static com.clustercontrol.ws.reporting.ReportingInfo
		getReportingInfoDto(ReportingInfo info) throws ParseException {
		
		com.clustercontrol.ws.reporting.ReportingInfo ret =
				new com.clustercontrol.ws.reporting.ReportingInfo();
		
		try {
			// 登録日時、更新日時に利用する日時（実行日時とする）
			long now = new Date().getTime();
			
			ret.setReportScheduleId(info.getReportScheduleId());
			ret.setDescription(info.getDescription());
			ret.setOwnerRoleId(info.getOwnerRoleId());
			ret.setFacilityId(info.getFacilityId());
			if (info.getCalendarId() != null){
				ret.setCalendarId(info.getCalendarId());
			}
			ret.setOutputPeriodType(info.getOutputPeriodType());
			ret.setOutputPeriodBefore(info.getOutputPeriodBefore());
			ret.setOutputPeriodFor(info.getOutputPeriodFor());
			ret.setTemplateSetId(info.getTemplateSetId());
			if (info.getReportTitle() != null) {
				ret.setReportTitle(info.getReportTitle());
			}
			ret.setLogoValidFlg(info.getLogoValidFlg());
			if (info.getLogoFilename() != null ){
				ret.setLogoFilename(info.getLogoFilename());
			}
			
			ret.setPageValidFlg(info.getPageValidFlg());
			ret.setOutputType(info.getOutputType());
			Schedule schedule = new Schedule();
			
			// schedule.setType(info.getScheduleType());
			// ・XMLのスケジュールタイプが「毎日(0)」 → DTOのスケジュールタイプは「1」、日は未設定
			// ・XMLのスケジュールタイプが「毎週(1)」 → DTOのスケジュールタイプは「2」、週を設定
			// ・XMLのスケジュールタイプが「毎月(2)」 → DTOのスケジュールタイプは「1」、日を設定
			switch (info.getScheduleType()) {
			case TYPE_EVERY_DAY:
				schedule.setType(com.clustercontrol.bean.ScheduleConstant.TYPE_DAY);
				break;
			case TYPE_EVERY_WEEK:
				schedule.setType(com.clustercontrol.bean.ScheduleConstant.TYPE_WEEK);
				if (info.hasWeek()){
					schedule.setWeek(info.getWeek());
				}
				break;
			case TYPE_EVERY_MONTH:
				schedule.setType(com.clustercontrol.bean.ScheduleConstant.TYPE_DAY);
				if (info.hasDay()){
					schedule.setDay(info.getDay());
				}
				break;
			default:
				break;
			}
			
			schedule.setHour(info.getHour());
			schedule.setMinute(info.getMinute());
			ret.setSchedule(schedule);

			List<NotifyRelationInfo> notifyRelationInfoList = new ArrayList<NotifyRelationInfo>();
			NotifyRelationInfo notifyRelationInfo = null;
			com.clustercontrol.utility.settings.report.xml.NotifyId[] notifies = info.getNotifyId();
			
			String notifyGroupId = ReportingUtil.createNotifyGroupIdReporting(info.getReportScheduleId());
			for (int i = 0; i < notifies.length; i++) {
				notifyRelationInfo = new NotifyRelationInfo();
				notifyRelationInfo.setNotifyGroupId(notifyGroupId);
				if ( notifies[i].getNotifyId() != null && ! notifies[i].getNotifyId().equals("")) {
					notifyRelationInfo.setNotifyId( notifies[i].getNotifyId());
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(NotifyRelation-NotifyId) : " + info.getNotifyGroupId());
					continue;
				}
				notifyRelationInfo.setNotifyType( notifies[i].getNotifyType());
				notifyRelationInfoList.add(notifyRelationInfo);
			}
			
			ret.getNotifyId().clear();
			ret.getNotifyId().addAll(notifyRelationInfoList);
			ret.setNotifyGroupId(notifyGroupId);
			
			ret.setValidFlg(info.getValidFlg());
			ret.setRegDate(now);
			ret.setRegUser(Config.getConfig("Login.USER"));
			ret.setUpdateDate(now);
			ret.setUpdateUser(Config.getConfig("Login.USER"));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return ret;
	}
}
