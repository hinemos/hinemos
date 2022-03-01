/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;
import java.util.ArrayList;

import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ScheduleTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.SessionPremakeEveryXHourEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.SessionPremakeScheduleTypeEnum;
import com.clustercontrol.rest.util.RestCommonConverter;
import com.clustercontrol.rest.util.RestItemNameResolver;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

public abstract class AbstractScheduleRequest implements RequestDto{

	/** 実行契機名 */
	private String name;

	/** ジョブID */
	private String jobId;

	/** ジョブユニットID */
	private String jobunitId;

	/** 有効/無効 */
	private Boolean valid = false;

	/** カレンダID */
	private String calendarId;

	/** ランタイムジョブ変数情報 */
	private ArrayList<JobRuntimeParamRequest> jobRuntimeParamList;

	/** スケジュール種別 */
	@RestBeanConvertEnum
	private ScheduleTypeEnum scheduleType;

	private Integer week;

	private Integer hour;

	private Integer minute;

	private Integer fromXminutes;

	private Integer everyXminutes;

	/** ジョブセッション事前生成 */
	private Boolean sessionPremakeFlg = false;

	@RestItemName(value = MessageConstant.SESSION_PRE_MAKE_SCHEDULE_TYPE)
	@RestBeanConvertEnum
	private SessionPremakeScheduleTypeEnum sessionPremakeScheduleType;

	@RestItemName(value = MessageConstant.SESSION_PRE_MAKE_WEEK)
	@RestValidateInteger(minVal=DayOfWeekConstant.TYPE_SUNDAY, maxVal=DayOfWeekConstant.TYPE_SATURDAY)
	private Integer sessionPremakeWeek;

	@RestItemName(value = MessageConstant.SESSION_PRE_MAKE_HOUR)
	@RestValidateInteger(minVal=0, maxVal=23)
	private Integer sessionPremakeHour;

	@RestItemName(value = MessageConstant.SESSION_PRE_MAKE_MINUTE)
	@RestValidateInteger(minVal=0, maxVal=59)
	private Integer sessionPremakeMinute;

	@RestItemName(value = MessageConstant.SESSION_PRE_MAKE_EVERY_X_HOUR)
	@RestBeanConvertEnum
	private SessionPremakeEveryXHourEnum sessionPremakeEveryXHour;

	@RestItemName(value = MessageConstant.SESSION_PRE_MAKE_DATE)
	@RestBeanConvertDatetime
	private String sessionPremakeDate;

	@RestItemName(value = MessageConstant.SESSION_PRE_MAKE_TO_DATE)
	@RestBeanConvertDatetime
	private String sessionPremakeToDate;

	private Boolean sessionPremakeInternalFlg;

	public AbstractScheduleRequest(){
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public ArrayList<JobRuntimeParamRequest> getJobRuntimeParamList() {
		return jobRuntimeParamList;
	}

	public void setJobRuntimeParamList(ArrayList<JobRuntimeParamRequest> jobRuntimeParamList) {
		this.jobRuntimeParamList = jobRuntimeParamList;
	}

	public ScheduleTypeEnum getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(ScheduleTypeEnum scheduleType) {
		this.scheduleType = scheduleType;
	}

	public Integer getWeek() {
		return week;
	}

	public void setWeek(Integer week) {
		this.week = week;
	}

	public Integer getHour() {
		return hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}

	public Integer getMinute() {
		return minute;
	}

	public void setMinute(Integer minute) {
		this.minute = minute;
	}

	public Integer getFromXminutes() {
		return fromXminutes;
	}

	public void setFromXminutes(Integer fromXminutes) {
		this.fromXminutes = fromXminutes;
	}

	public Integer getEveryXminutes() {
		return everyXminutes;
	}

	public void setEveryXminutes(Integer everyXminutes) {
		this.everyXminutes = everyXminutes;
	}

	public Boolean getSessionPremakeFlg() {
		return sessionPremakeFlg;
	}

	public void setSessionPremakeFlg(Boolean sessionPremakeFlg) {
		this.sessionPremakeFlg = sessionPremakeFlg;
	}

	public SessionPremakeScheduleTypeEnum getSessionPremakeScheduleType() {
		return sessionPremakeScheduleType;
	}

	public void setSessionPremakeScheduleType(SessionPremakeScheduleTypeEnum sessionPremakeScheduleType) {
		this.sessionPremakeScheduleType = sessionPremakeScheduleType;
	}

	public Integer getSessionPremakeWeek() {
		return sessionPremakeWeek;
	}

	public void setSessionPremakeWeek(Integer sessionPremakeWeek) {
		this.sessionPremakeWeek = sessionPremakeWeek;
	}

	public Integer getSessionPremakeHour() {
		return sessionPremakeHour;
	}

	public void setSessionPremakeHour(Integer sessionPremakeHour) {
		this.sessionPremakeHour = sessionPremakeHour;
	}

	public Integer getSessionPremakeMinute() {
		return sessionPremakeMinute;
	}

	public void setSessionPremakeMinute(Integer sessionPremakeMinute) {
		this.sessionPremakeMinute = sessionPremakeMinute;
	}

	public SessionPremakeEveryXHourEnum getSessionPremakeEveryXHour() {
		return sessionPremakeEveryXHour;
	}

	public void setSessionPremakeEveryXHour(SessionPremakeEveryXHourEnum sessionPremakeEveryXHour) {
		this.sessionPremakeEveryXHour = sessionPremakeEveryXHour;
	}

	public String getSessionPremakeDate() {
		return sessionPremakeDate;
	}

	public void setSessionPremakeDate(String sessionPremakeDate) {
		this.sessionPremakeDate = sessionPremakeDate;
	}

	public String getSessionPremakeToDate() {
		return sessionPremakeToDate;
	}

	public void setSessionPremakeToDate(String sessionPremakeToDate) {
		this.sessionPremakeToDate = sessionPremakeToDate;
	}

	public Boolean getSessionPremakeInternalFlg() {
		return sessionPremakeInternalFlg;
	}

	public void setSessionPremakeInternalFlg(Boolean sessionPremakeInternalFlg) {
		this.sessionPremakeInternalFlg = sessionPremakeInternalFlg;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		if (sessionPremakeFlg) {
			if (scheduleType == ScheduleTypeEnum.INTERVAL) {
				throw new InvalidSetting(MessageConstant.MESSAGE_JOBSCHEDULE_NOT_SELECT_INTERVAL_PREMAKE.getMessage());
			}
			if (sessionPremakeScheduleType == SessionPremakeScheduleTypeEnum.EVERY_DAY
					|| sessionPremakeScheduleType == SessionPremakeScheduleTypeEnum.EVERY_WEEK
					|| sessionPremakeScheduleType == SessionPremakeScheduleTypeEnum.TIME) {
				if (sessionPremakeHour == null) {
					String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "sessionPremakeHour");
					throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
				}
				if (sessionPremakeMinute == null) {
					String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "sessionPremakeMinute");
					throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
				}
			}
			if (sessionPremakeScheduleType == SessionPremakeScheduleTypeEnum.EVERY_WEEK) {
				if (sessionPremakeWeek == null) {
					String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "sessionPremakeWeek");
					throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
				}
			}
			if (sessionPremakeScheduleType == SessionPremakeScheduleTypeEnum.TIME) {
				if (sessionPremakeEveryXHour == null) {
					String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "sessionPremakeEveryXHour");
					throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
				}
			}
			if (sessionPremakeScheduleType == SessionPremakeScheduleTypeEnum.DATETIME) {
				String sessionPremakeDateName = RestItemNameResolver.resolveItenName(this.getClass(), "sessionPremakeDate");
				String sessionPremakeToDateName = RestItemNameResolver.resolveItenName(this.getClass(), "sessionPremakeToDate");
				if (sessionPremakeDate == null) {
					throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(sessionPremakeDateName));
				}
				if (sessionPremakeToDate == null) {
					throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(sessionPremakeToDateName));
				}
				Long sessionPremakeDateValue = RestCommonConverter.convertDTStringToHinemosTime(sessionPremakeDate, sessionPremakeDateName);
				Long sessionPremakeToDateValue = RestCommonConverter.convertDTStringToHinemosTime(sessionPremakeToDate, sessionPremakeToDateName);
				// sessionPremakeDate <= sessionPremakeToDateであること
				if (sessionPremakeDateValue > sessionPremakeToDateValue) {
					throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_LATER_DATE_AND_TIME.getMessage(
							sessionPremakeToDateName, sessionPremakeDateName));
				}
				// 現在日時 <= sessionPremakeDateであること
				if (HinemosTime.currentTimeMillis() > sessionPremakeDateValue) {
					throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_LATER_DATE_AND_TIME.getMessage(
							sessionPremakeDateName, MessageConstant.CURRENT_DATETIME.getMessage()));
				}
			}
		}
	}
}
