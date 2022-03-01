/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.calendar.model.YMD;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.reporting.model.ReportingInfoEntity;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * カレンダの入力チェッククラス
 * 
 * @since 4.0
 */
public class CalendarValidator {

	private static Log m_log = LogFactory.getLog(CalendarValidator.class);
	
	// カレンダ詳細で設定する開始時刻,終了時刻の最大,最小値
	private static final long DATETIME_VALUE_MIN = -392399000L; //「-99:59:59」のエポック秒
	private static final long DATETIME_VALUE_MAX = 3567599000L; //「999:59:59」のエポック秒
	private static final String DATETIME_STRING_MIN = "-99:59:59"; //日時下限越えエラー通知用文字列
	private static final String DATETIME_STRING_MAX = "999:59:59"; //日時上限越えエラー通知用文字列
	
	/**
	 * カレンダ情報(CalendarInfo)の基本設定の妥当性チェック
	 * 
	 * @param calendarInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateCalendarInfo(CalendarInfo calendarInfo) throws InvalidSetting, InvalidRole {
		// calendarId
		CommonValidator.validateId(MessageConstant.CALENDAR_ID.getMessage(), calendarInfo.getCalendarId(), 64);

		// calendarName
		CommonValidator.validateString(MessageConstant.CALENDAR_NAME.getMessage(), calendarInfo.getCalendarName(), true, 1, 256);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(calendarInfo.getOwnerRoleId(), true, calendarInfo.getCalendarId(), HinemosModuleConstant.PLATFORM_CALENDAR);
		
		// description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), calendarInfo.getDescription(), false, 0, 256);

		if (calendarInfo.getValidTimeFrom() == null || calendarInfo.getValidTimeFrom() == 0) {
			m_log.warn("validateCalendarInfo() " + MessageConstant.START.getMessage());
			String[] args = { "(" + MessageConstant.START.getMessage() + ")" };
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_VALIDITY_PERIOD.getMessage(args));
		}
		if (calendarInfo.getValidTimeTo() == null || calendarInfo.getValidTimeTo() == 0) {
			m_log.warn("validateCalendarInfo() " + MessageConstant.END.getMessage());
			String[] args = { "(" + MessageConstant.END.getMessage() + ")" };
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_VALIDITY_PERIOD.getMessage(args));
		}
		if (calendarInfo.getValidTimeFrom() >= calendarInfo.getValidTimeTo()) {
			m_log.warn("validateCalendarInfo() " + MessageConstant.END.getMessage());
			String[] args = { MessageConstant.TIME.getMessage() + "(" + MessageConstant.END.getMessage() + ")",
					MessageConstant.TIME.getMessage() + "(" + MessageConstant.START.getMessage() + ")" };
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_LATER_DATE_AND_TIME.getMessage(args));
		}
		//カレンダ詳細チェック
		for(CalendarDetailInfo detailInfo : calendarInfo.getCalendarDetailList()){
			validdateCalendarDetailInfo(detailInfo, calendarInfo.getOwnerRoleId());
		}

	}
	/**
	 * カレンダ詳細情報（CalendarDetailInfo）の基本設定の妥当性チェック
	 * @param detailInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	private static void validdateCalendarDetailInfo(CalendarDetailInfo detailInfo, String ownerRoleId) throws InvalidSetting, InvalidRole {
		//説明
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), detailInfo.getDescription(), false, 0, 256);
		
		//年は必須項目のためチェック
		if (detailInfo.getYear() == null || detailInfo.getYear() < 0) {
			String[] args = { "(" + MessageConstant.YEAR.getMessage() + ")" };
			m_log.warn("ValidYear:" + args[0]);
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CALENDAR_DETAIL.getMessage(Arrays.toString(args)));
		}
		//月は必須項目のためチェック（コンボボックス入力だが、一応）
		if (detailInfo.getMonth() == null || detailInfo.getMonth() < 0) {
			String[] args = { "(" + MessageConstant.MONTH.getMessage() + ")" };
			m_log.warn("ValidMonth:" + Arrays.toString(args));
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CALENDAR_DETAIL.getMessage(Arrays.toString(args)));
		}
		//日は必須項目のためチェック
		if (detailInfo.getDayType() == null || detailInfo.getDayType() < 0 || detailInfo.getDayType() > 3) {
			String[] args = { "(" + MessageConstant.CALENDAR_DETAIL_DATE_TYPE.getMessage() + ")" };
			m_log.warn("ValidDateType:" + Arrays.toString(args));
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CALENDAR_DETAIL.getMessage(Arrays.toString(args)));
		}
		//日タイプが「1」の場合、第x週、曜日が必須項目となるためチェック
		if(detailInfo.getDayType() == 1){
			if(detailInfo.getDayOfWeekInMonth() == null || detailInfo.getDayOfWeekInMonth() < 0){
				String[] args = { "(" + MessageConstant.CALENDAR_DETAIL_XTH.getMessage() + ")" };
				m_log.warn("ValidXth:" + Arrays.toString(args));
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CALENDAR_DETAIL.getMessage(Arrays.toString(args)));
			}
			if(detailInfo.getDayOfWeek() == null || detailInfo.getDayOfWeek() < 1 || detailInfo.getDayOfWeek() > 7){
				String[] args = { "(" + MessageConstant.WEEKDAY.getMessage() + ")" };
				m_log.warn("ValidWeekDay:" + Arrays.toString(args));
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CALENDAR_DETAIL.getMessage(Arrays.toString(args)));
			}
		}
		//日タイプが「2」の場合、日が必須項目となるためチェック
		if(detailInfo.getDayType() == 2){
			if(detailInfo.getDate() == null || detailInfo.getDate() < 0){
				String[] args = { "(" +MessageConstant.MONTHDAY.getMessage() + ")" };
				m_log.warn("ValidMonthDay:" + Arrays.toString(args));
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CALENDAR_DETAIL.getMessage(Arrays.toString(args)));
			}
		}
		//日タイプが「3」の場合、カレンダパターンが必須項目となるためチェック
		if(detailInfo.getDayType() == 3){
			if(detailInfo.getCalPatternId() == null){
				String[] args = { "(" + MessageConstant.CALENDAR_PATTERN.getMessage() + ")" };
				m_log.warn("ValidCalendarPattern:" + Arrays.toString(args));
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CALENDAR_DETAIL.getMessage(Arrays.toString(args)));
			}
			//IDと一致するカレンダパターン情報が存在しない場合
			try {
				CalendarPatternCache.getCalendarPatternInfo(detailInfo.getCalPatternId());
				QueryUtil.getCalPatternInfoPK_OR(detailInfo.getCalPatternId(), ownerRoleId);
			} catch (CalendarNotFound e) {
				String[] args = { "(" + detailInfo.getCalPatternId() + ")" };
				m_log.warn("ValidCalendarPattern:" + Arrays.toString(args));
				throw new InvalidSetting(MessageConstant.MESSAGE_FILE_NOT_FOUND.getMessage(Arrays.toString(args)));
			} catch (InvalidRole e) {
				m_log.warn("ValidCalendarPattern: "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		
		CommonValidator.validateNull(MessageConstant.CALENDAR_DETAIL_BEFORE_AFTER.getMessage(), detailInfo.getAfterday());
		
		if (-32768 > detailInfo.getAfterday() || detailInfo.getAfterday() > 32767) {
			String[] args = {MessageConstant.CALENDAR_DETAIL_BEFORE_AFTER.getMessage(),
					"-32768", "32767"};
			m_log.warn("ValidAfterDay:" + Arrays.toString(args));
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_RANGE.getMessage(Arrays.toString(args)));
		}
		
		// 振り替え
		CommonValidator.validateNull(MessageConstant.CALENDAR_DETAIL_SUBSTITUTE_FLAG.getMessage(), detailInfo.isSubstituteFlg());
		
		// 振り替え間隔と振り替え上限が未入力の場合はエラーにする
		if (detailInfo.getSubstituteTime() != null) {
			if (detailInfo.getSubstituteTime() == 0 || detailInfo.getSubstituteTime() < (-24*366) || detailInfo.getSubstituteTime() > (24*366)) {
				String[] args = {MessageConstant.CALENDAR_DETAIL_SUBSTITUTE_TIME.getMessage(), "0", String.valueOf((-24*366)), String.valueOf((24*366))};
				m_log.warn("ValidSubstituteTime:" + Arrays.toString(args));
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_VALUE_AND_RANGE.getMessage(Arrays.toString(args)));
			}
		} else {
			String[] args = {MessageConstant.CALENDAR_DETAIL_SUBSTITUTE_TIME.getMessage()};
			m_log.warn("ValidSubstituteTime:" + Arrays.toString(args));
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CALENDAR_DETAIL.getMessage(Arrays.toString(args)));
		}
		if (detailInfo.getSubstituteLimit() != null) {
			if (detailInfo.getSubstituteLimit() < 1 || detailInfo.getSubstituteLimit() > 99) {
				String[] args = {MessageConstant.CALENDAR_DETAIL_SUBSTITUTE_LIMIT.getMessage(), "1", "99"};
				m_log.warn("ValidSubstituteLimit:" + Arrays.toString(args));
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_RANGE.getMessage(Arrays.toString(args)));
			}
		} else {
			String[] args = {MessageConstant.CALENDAR_DETAIL_SUBSTITUTE_LIMIT.getMessage()};
			m_log.warn("ValidSubstituteLimit:" + Arrays.toString(args));
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CALENDAR_DETAIL.getMessage(Arrays.toString(args)));
		}
		
////	if(cal.getStartTime() != null){
////	info.setTimeFrom(cal.getStartTime().getTime());
////}
//////終了時間
////if(cal.getEndTime() != null){
////	info.setTimeTo(cal.getEndTime().getTime());
		
		
		//時間：開始時間、終了時間は必須項目のためチェック
		if (detailInfo.getTimeFrom() == null) {
			String[] args = { "(" + MessageConstant.START.getMessage() + ")" };
			m_log.warn("ValidTimeFrom :" + Arrays.toString(args));
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_TIME.getMessage(Arrays.toString(args)));
		}
		if(detailInfo.getTimeFrom() < DATETIME_VALUE_MIN || DATETIME_VALUE_MAX < detailInfo.getTimeFrom()){
			String[] args = {DATETIME_STRING_MIN, DATETIME_STRING_MAX};
			m_log.warn("ValidTimeFrom :" + Arrays.toString(args));
			throw new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER.getMessage(args));
		}
		if (detailInfo.getTimeTo() == null) {
			String[] args = { "(" + MessageConstant.END.getMessage() + ")" };
			m_log.warn("ValidTimeTo :" + Arrays.toString(args));
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_TIME.getMessage(Arrays.toString(args)));
		}
		if(detailInfo.getTimeTo() < DATETIME_VALUE_MIN || DATETIME_VALUE_MAX < detailInfo.getTimeTo()){
			String[] args = {DATETIME_STRING_MIN, DATETIME_STRING_MAX};
			m_log.warn("ValidTimeTo :" + Arrays.toString(args));
			throw new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER.getMessage(args));
		}
		//終了時間が開始時間より過去に設定されてはならないため、チェック
		if (detailInfo.getTimeFrom() >= detailInfo.getTimeTo()) {
			String[] args = { MessageConstant.TIME.getMessage() + "(" + MessageConstant.END.getMessage() + ")",
					MessageConstant.TIME.getMessage() + "(" + MessageConstant.START.getMessage() + ")" };
			m_log.warn("ValidFromTo:" + Arrays.toString(args));
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_LATER_TIME.getMessage(Arrays.toString(args)));
		}
	}
	/**
	 * カレンダパターン情報（CalendarPatternInfo）の妥当性チェック
	 * @param CalendarPatternInfo
	 * @throws InvalidSetting
	 */
	public static void validateCalendarPatternInfo(CalendarPatternInfo info) throws InvalidSetting{

		// calendarPatternId
		CommonValidator.validateId(MessageConstant.CALENDAR_PATTERN_ID.getMessage(), info.getCalPatternId(), 64);

		// calendarPatternName
		CommonValidator.validateString(MessageConstant.CALENDAR_PATTERN_NAME.getMessage(), info.getCalPatternName(), true, 1, 128);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(info.getOwnerRoleId(), true,
				info.getCalPatternId(), HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN);

		//カレンダパターン詳細チェック
		for(YMD ymd : info.getYmd()){
			validateYMD(ymd);
		}
	}

	/**
	 * YMD
	 * YearMonthDayの妥当性チェック
	 * @param ymd
	 * @throws InvalidSetting
	 */
	public static void validateYMD(YMD ymd) throws InvalidSetting{
		Integer year = ymd.getYear();
		Integer month = ymd.getMonth();
		Integer day = ymd.getDay();

		if(year == null){
			String[] args = { "(" + MessageConstant.YEAR.getMessage() + ")" };
			m_log.warn("validateYMD year=null");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CALENDAR_PATTERN.getMessage(args));
		}
		if(month == null || month <= 0){
			String[] args = { "(" + MessageConstant.MONTH.getMessage() + ")" };
			m_log.warn("validateYMD month=null");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CALENDAR_PATTERN.getMessage(args));
		}
		if(day == null || day <= 0){
			String[] args = { "(" + MessageConstant.DAY.getMessage() + ")" };
			m_log.warn("validateYMD day + month");
			throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CALENDAR_PATTERN.getMessage(args));
		}
		//存在する年月日かチェック
		boolean ret = false;
		Calendar cal = HinemosTime.getCalendarInstance();
		cal.setLenient( true );
		cal.set(year, month - 1, day);
		if (cal.get(Calendar.MONTH) != (month - 1) % 12) {
			// error
			m_log.warn("year=" + year + ",month=" + month + ",day=" + day + ",ret=" + ret);
			String[] args = { year + "/" + month + "/" + day };
			throw new InvalidSetting(MessageConstant.MESSAGE_SCHEDULE_NOT_EXIST.getMessage(args));
		}
		m_log.debug("year=" + year + ",month=" + month + ",day=" + day + ",ret=" + ret);
	}

	/**
	 * 他の機能にて、カレンダが参照状態であるか調査する。
	 * 参照状態の場合、メッセージダイアログが出力される。
	 * @param calendarId
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void valideDeleteCalendar(String calendarId) throws InvalidSetting, HinemosUnknown{
		try{
			//ジョブ
			List<JobMstEntity> jobMstList =
					com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstEntityFindByCalendarId(calendarId);
			if (jobMstList != null) {
				for(JobMstEntity jobMst : jobMstList){
					m_log.debug("valideDeleteCalendar() target JobMaster " + jobMst.getId().getJobId() + ", calendarId = " + calendarId);
					if(jobMst.getCalendarId() != null){
						String[] args = {jobMst.getId().getJobId(),calendarId};
						throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_JOB_REFERENCE.getMessage(args));
					}
				}
			}
			//ジョブ実行契機
			List<JobKickEntity> jobKickList =
					com.clustercontrol.jobmanagement.util.QueryUtil.getJobKickEntityFindByCalendarId_NONE(calendarId);
			if (jobKickList != null) {
				for(JobKickEntity jobKick : jobKickList){
					m_log.debug("valideDeleteCalendar() target jobkick " + jobKick.getJobkickId() + ", calendarId = " + calendarId);
					if(jobKick.getCalendarId() != null){
						String[] args = {jobKick.getJobkickId(),calendarId};
						if (jobKick.getJobkickType().equals(JobKickConstant.TYPE_FILECHECK)) {
							throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_JOBFILECHECK_REFERENCE.getMessage(args));
						} else if (jobKick.getJobkickType().equals(JobKickConstant.TYPE_SCHEDULE)) {
							throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_JOBSCHEDULE_REFERENCE.getMessage(args));
						}
					}
				}
			}
			//監視設定
			List<MonitorInfo> monitorList =
					com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoFindByCalendarId_NONE(calendarId);
			if (monitorList != null) {
				for(MonitorInfo monitorInfo : monitorList){
					m_log.debug("valideDeleteCalendar() target MonitorInfo " + monitorInfo.getMonitorId() + ", calendarId = " + calendarId);
					if(monitorInfo.getCalendarId() != null){
						String[] args = {monitorInfo.getMonitorId(),calendarId};
						throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_MONITOR_REFERENCE.getMessage(args));
					}
				}
			}
			//メンテナンス
			List<MaintenanceInfo> maintenanceInfoList =
					com.clustercontrol.maintenance.util.QueryUtil.getMaintenanceInfoFindByCalendarId_NONE(calendarId);
			if (maintenanceInfoList != null) {
				for(MaintenanceInfo maintenanceInfo : maintenanceInfoList){
					m_log.debug("valideDeleteCalendar() target MaintenanceInfo " + maintenanceInfo.getMaintenanceId() + ", calendarId = " + calendarId);
					if(maintenanceInfo.getCalendarId() != null){
						String[] args = {maintenanceInfo.getMaintenanceId(),calendarId};
						throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_MAINTENANCE_REFERENCE.getMessage(args));
					}
				}
			}
			//通知
			List<NotifyInfo> notifyInfoList =
					com.clustercontrol.notify.util.QueryUtil.getNotifyInfoFindByCalendarId_NONE(calendarId);
			if (notifyInfoList != null) {
				for(NotifyInfo notifyInfo : notifyInfoList){
					m_log.debug("valideDeleteCalendar() target MaintenanceInfo " + notifyInfo.getNotifyId() + ", calendarId = " + calendarId);
					if(notifyInfo.getCalendarId() != null){
						String[] args = {notifyInfo.getNotifyId(), calendarId};
						throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_NOTIFY_REFERENCE.getMessage(args));
					}
				}
			}
			//構成情報取得設定
			List<NodeConfigSettingInfo> nodeConfigSettingInfoList =
					com.clustercontrol.repository.util.QueryUtil.getNodeConfigSettingInfoFindByCalendarId_NONE(calendarId);
			if (nodeConfigSettingInfoList != null) {
				for(NodeConfigSettingInfo nodeConfigSettingInfo : nodeConfigSettingInfoList){
					m_log.debug("valideDeleteCalendar() target NodeConfigSettingInfo " + nodeConfigSettingInfo.getSettingId() + ", calendarId = " + calendarId);
					if(nodeConfigSettingInfo.getCalendarId() != null){
						String[] args = {nodeConfigSettingInfo.getSettingId(), calendarId};
						throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_NODECONFIG_REFERENCE.getMessage(args));
					}
				}
			}
			//レポーティング設定
			List<ReportingInfoEntity> reportingList =
					com.clustercontrol.reporting.util.QueryUtil.getReportingInfoFindByCalendarId_NONE(calendarId);
			if (reportingList != null) {
				for(ReportingInfoEntity reportingInfo : reportingList){
					m_log.debug("valideDeleteCalendar() target ReportingInfoEntity " + reportingInfo.getReportScheduleId() + ", calendarId = " + calendarId);
					if(reportingInfo.getCalendarId() != null){
						String[] args = {reportingInfo.getReportScheduleId(),calendarId};
						throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_REPORTING_SCHEDULE_REFERENCE.getMessage(args));
					}
				}
			}
			//収集蓄積転送設定
			List<TransferInfo> transferInfoList =
					com.clustercontrol.hub.util.QueryUtil.getTransferInfoFindByCalendarId_NONE(calendarId);
			if (transferInfoList != null) {
				for(TransferInfo transferInfo : transferInfoList){
					m_log.debug("valideDeleteCalendar() target TransferInfo " + transferInfo.getTransferId() + ", calendarId = " + calendarId);
					if(transferInfo.getCalendarId() != null){
						String[] args = {transferInfo.getTransferId(),calendarId};
						throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_HUB_TRANSFER_REFERENCE.getMessage(args));
					}
				}
			}
			
		} catch (InvalidSetting e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("valideDeleteCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}
	/**
	 * 削除対象のカレンダパターンがカレンダにて使用中の場合、
	 * DBコミット前に、メッセージダイアログを出力し中止する
	 * @param calPatternId
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void valideDeleteCalendarPattern(String calPatternId) throws InvalidSetting, HinemosUnknown{
		List<CalendarDetailInfo> calDetailList = null;
		try{
			//カレンダパターンIDと一致するカレンダ詳細情報を取得する
			calDetailList = QueryUtil.getCalDetailByCalPatternId(calPatternId);
			/*
			 * カレンダパターンIDと一致したカレンダ詳細情報を取得した場合に、
			 * メッセージダイアログ出力する
			 * nullの場合は、何もせずにreturn
			 */
			if(calDetailList != null){
				for(CalendarDetailInfo calDtail : calDetailList){
					m_log.warn("valideDeleteCalendarPattern() target CalendarDetailInfo " + calDtail.getId().getCalendarId() + ", calendarId = " + calPatternId);
					if(calDtail.getCalPatternId() != null){
						String[] args = {calDtail.getCalPatternId(),calPatternId};
						throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_CALENDAR_REFERENCE.getMessage(args));
					}
				}
			}
		} catch (InvalidSetting e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("valideDeleteCalendar() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}
}
