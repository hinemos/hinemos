/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.bean;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlType;


/**
 * スケジュール情報のBeanクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
@XmlType(namespace = "http://common.ws.clustercontrol.com")
@Embeddable
public class Schedule implements Serializable {

	private static final long serialVersionUID = -4329164675366707838L;

	public Schedule() {}

	public Schedule(int type, Integer month, Integer day, Integer week,
			Integer hour, Integer minute) {
		super();
		this.type = type;
		this.month = month;
		this.day = day;
		this.week = week;
		this.hour = hour;
		this.minute = minute;
	}

	/** スケジュール種別
	 * @see com.clustercontrol.bean.ScheduleConstant
	 * */
	private int type;

	private Integer month = null;

	private Integer day = null;

	private Integer week = null;

	private Integer hour = null;

	private Integer minute = null;

	@Column(name="schedule_type")
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Column(name="month")
	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	@Column(name="day")
	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	@Column(name="week")
	public Integer getWeek() {
		return week;
	}

	public void setWeek(Integer week) {
		this.week = week;
	}

	@Column(name="hour")
	public Integer getHour() {
		return hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}

	@Column(name="minute")
	public Integer getMinute() {
		return minute;
	}

	public void setMinute(Integer minute) {
		this.minute = minute;
	}

	@Override
	public String toString() {
		String str = null;
		str += "type=" + type;
		str += " ,month=" + month;
		str += " ,day=" + day;
		str += " ,week=" + week;
		str += " ,hour=" + hour;
		str += " ,minute=" + minute;
		return str;
	}
}