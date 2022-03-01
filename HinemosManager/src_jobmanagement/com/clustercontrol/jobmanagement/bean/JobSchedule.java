/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
	private int scheduleType;

	private Integer week = null;

	private Integer hour = null;

	private Integer minute = null;

	private Integer fromXminutes = null;

	private Integer everyXminutes = null;

	/** ジョブセッション事前生成フラグ */
	private Boolean sessionPremakeFlg;

	/** セッション事前生成スケジュールタイプ */
	private Integer sessionPremakeScheduleType;

	/** 事前生成スケジュール - 曜日 */
	private Integer sessionPremakeWeek;

	/** 事前生成スケジュール - 時 */
	private Integer sessionPremakeHour;

	/** 事前生成スケジュール - 分 */
	private Integer sessionPremakeMinute;

	/** 事前生成スケジュール - X時間ごと */
	private Integer sessionPremakeEveryXHour;

	/** 事前生成スケジュール - 日時(生成タイミング) */
	private Long sessionPremakeDate;

	/** 事前生成スケジュール - 日時(生成期間) */
	private Long sessionPremakeToDate;

	/** 事前生成INTERNALイベントフラグ */
	private Boolean sessionPremakeInternalFlg;

	public JobSchedule() {
		this.type = JobKickConstant.TYPE_SCHEDULE;
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
		this.type = JobKickConstant.TYPE_SCHEDULE;
		this.scheduleType = scheduleType;
		this.week = week;
		this.hour = hour;
		this.minute = minute;
		this.fromXminutes = fromXminutes;
		this.everyXminutes = everyXminutes;
	}

	public int getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(int scheduleType) {
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

	/**
	 * ジョブセッション事前生成フラグ取得
	 * 
	 * @return ジョブセッション事前生成フラグ
	 */
	public Boolean getSessionPremakeFlg() {
		return sessionPremakeFlg;
	}

	/**
	 * ジョブセッション事前生成フラグ設定
	 * 
	 * @param sessionPremakeFlg ジョブセッション事前生成フラグ
	 */
	public void setSessionPremakeFlg(Boolean sessionPremakeFlg) {
		this.sessionPremakeFlg = sessionPremakeFlg;
	}

	/**
	 * セッション事前生成スケジュールタイプ取得
	 * 
	 * @return セッション事前生成スケジュールタイプ
	 */
	public Integer getSessionPremakeScheduleType() {
		return sessionPremakeScheduleType;
	}

	/**
	 * セッション事前生成スケジュールタイプ設定
	 * 
	 * @param sessionPremakeScheduleType セッション事前生成スケジュールタイプ
	 */
	public void setSessionPremakeScheduleType(Integer sessionPremakeScheduleType) {
		this.sessionPremakeScheduleType = sessionPremakeScheduleType;
	}

	/**
	 * 事前生成スケジュール（曜日）取得
	 * 
	 * @return 事前生成スケジュール（曜日）
	 */
	public Integer getSessionPremakeWeek() {
		return sessionPremakeWeek;
	}

	/**
	 * 事前生成スケジュール（曜日）設定
	 * 
	 * @param sessionPremakeWeek 事前生成スケジュール（曜日）
	 */
	public void setSessionPremakeWeek(Integer sessionPremakeWeek) {
		this.sessionPremakeWeek = sessionPremakeWeek;
	}

	/**
	 * 事前生成スケジュール（時）取得
	 * 
	 * @return 事前生成スケジュール（時）
	 */
	public Integer getSessionPremakeHour() {
		return sessionPremakeHour;
	}

	/**
	 * 事前生成スケジュール（時）設定
	 * 
	 * @param sessionPremakeHour 事前生成スケジュール（時）
	 */
	public void setSessionPremakeHour(Integer sessionPremakeHour) {
		this.sessionPremakeHour = sessionPremakeHour;
	}

	/**
	 * 事前生成スケジュール（分）取得
	 * 
	 * @return 事前生成スケジュール（分）
	 */
	public Integer getSessionPremakeMinute() {
		return sessionPremakeMinute;
	}

	/**
	 * 事前生成スケジュール（分）設定
	 * 
	 * @param sessionPremakeMinute 事前生成スケジュール（分）
	 */
	public void setSessionPremakeMinute(Integer sessionPremakeMinute) {
		this.sessionPremakeMinute = sessionPremakeMinute;
	}

	/**
	 * 事前生成スケジュール（x時間ごと）取得
	 * 
	 * @return 事前生成スケジュール（x時間ごと）
	 */
	public Integer getSessionPremakeEveryXHour() {
		return sessionPremakeEveryXHour;
	}

	/**
	 * 事前生成スケジュール（x時間ごと）設定
	 * 
	 * @param sessionPremakeEveryXHour 事前生成スケジュール（x時間ごと）
	 */
	public void setSessionPremakeEveryXHour(Integer sessionPremakeEveryXHour) {
		this.sessionPremakeEveryXHour = sessionPremakeEveryXHour;
	}

	/**
	 * 事前生成スケジュール（日時－生成タイミング）取得
	 * 
	 * @return 事前生成スケジュール（日時－生成タイミング）
	 */
	public Long getSessionPremakeDate() {
		return sessionPremakeDate;
	}

	/**
	 * 事前生成スケジュール（日時－生成タイミング）設定
	 * 
	 * @param sessionPremakeDate 事前生成スケジュール（日時－生成タイミング）
	 */
	public void setSessionPremakeDate(Long sessionPremakeDate) {
		this.sessionPremakeDate = sessionPremakeDate;
	}

	/**
	 * 事前生成スケジュール（日時－生成期間）取得
	 * 
	 * @return 事前生成スケジュール（日時－生成期間）
	 */
	public Long getSessionPremakeToDate() {
		return sessionPremakeToDate;
	}

	/**
	 * 事前生成スケジュール（日時－生成期間）設定
	 * 
	 * @param sessionPremakeToDate 事前生成スケジュール（日時－生成期間）
	 */
	public void setSessionPremakeToDate(Long sessionPremakeToDate) {
		this.sessionPremakeToDate = sessionPremakeToDate;
	}

	/**
	 * 事前生成INTERNALイベントフラグ取得
	 * 
	 * @return 事前生成INTERNALイベントフラグ
	 */
	public Boolean getSessionPremakeInternalFlg() {
		return sessionPremakeInternalFlg;
	}

	/**
	 * 事前生成INTERNALイベントフラグ設定
	 * 
	 * @param sessionPremakeInternalFlg 事前生成INTERNALイベントフラグ
	 */
	public void setSessionPremakeInternalFlg(Boolean sessionPremakeInternalFlg) {
		this.sessionPremakeInternalFlg = sessionPremakeInternalFlg;
	}

	@Override
	public String toString() {
		String str = null;
		str += "m_type=" + type;
		str += "m_scheduleType=" + scheduleType;
		str += " ,m_week=" + week;
		str += " ,m_hour=" + hour;
		str += " ,m_minute=" + minute;
		str += " ,m_fromXminutes=" + fromXminutes;
		str += " ,m_everyXminutes=" + everyXminutes;
		return str;
	}
}