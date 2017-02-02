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

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;


/**
 * ジョブ実行契機[スケジュール]に関する情報を保持するクラス<BR>
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobSchedule extends JobKick implements Serializable {

	/**
	 * シリアライズ可能クラスに定義するUID
	 */
	private static final long serialVersionUID = 9120046546824522587L;

	/** スケジュール種別
	 * @see com.clustercontrol.bean.ScheduleConstant
	 * */
	private int m_scheduleType;

	private Integer m_week = null;

	private Integer m_hour = null;

	private Integer m_minute = null;

	private Integer m_fromXminutes = null;

	private Integer m_everyXminutes = null;

	
	public JobSchedule() {
		this.m_type = JobKickConstant.TYPE_SCHEDULE;
	}
	/**
	 * スケジュール設定定義
	 * @param scheduleType
	 * @param week
	 * @param hour
	 * @param minute
	 * @param fromXminutes
	 * @param everyXminutes
	 */
	public JobSchedule(int scheduleType, Integer week,
			Integer hour, Integer minute, Integer fromXminutes, Integer everyXminutes) {
		super();
		this.m_type = JobKickConstant.TYPE_SCHEDULE;
		this.m_scheduleType = scheduleType;
		this.m_week = week;
		this.m_hour = hour;
		this.m_minute = minute;
		this.m_fromXminutes = fromXminutes;
		this.m_everyXminutes = everyXminutes;
	}

	public int getScheduleType() {
		return m_scheduleType;
	}

	public void setScheduleType(int scheduleType) {
		this.m_scheduleType = scheduleType;
	}

	public Integer getWeek() {
		return m_week;
	}

	public void setWeek(Integer week) {
		this.m_week = week;
	}

	public Integer getHour() {
		return m_hour;
	}

	public void setHour(Integer hour) {
		this.m_hour = hour;
	}

	public Integer getMinute() {
		return m_minute;
	}

	public void setMinute(Integer minute) {
		this.m_minute = minute;
	}

	public Integer getFromXminutes() {
		return m_fromXminutes;
	}

	public void setFromXminutes(Integer fromXminutes) {
		this.m_fromXminutes = fromXminutes;
	}

	public Integer getEveryXminutes() {
		return m_everyXminutes;
	}

	public void setEveryXminutes(Integer everyXminutes) {
		this.m_everyXminutes = everyXminutes;
	}

	@Override
	public String toString() {
		String str = null;
		str += "m_type=" + m_type;
		str += "m_scheduleType=" + m_scheduleType;
		str += " ,m_week=" + m_week;
		str += " ,m_hour=" + m_hour;
		str += " ,m_minute=" + m_minute;
		str += " ,m_fromXminutes=" + m_fromXminutes;
		str += " ,m_everyXminutes=" + m_everyXminutes;
		return str;
	}
}