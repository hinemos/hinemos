/*

Copyright (C) 2013 NTT DATA Corporation

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
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.calendar.model.CalendarInfo;


/**
 * ジョブ実行契機に関する情報を保持するクラス<BR>
 * @version 4.1.0
 * @since 4.1.0
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobKick implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -8260092064493407140L;

	/**
	 * 実行契機種別
	 */
	protected int m_type;

	/** 実行契機ID */
	private String m_id;

	/** 実行契機名 */
	private String m_name;

	/** ジョブID */
	private String m_jobId;

	/** ジョブ名 */
	private String m_jobName;

	/** ジョブユニットID */
	private String m_jobunitId;

	/** オーナーロールID */
	private String m_ownerRoleId;

	/** 新規作成ユーザ */
	private String m_createUser;

	/** 作成日時 */
	private Long m_createTime;

	/** 最新更新ユーザ */
	private String m_updateUser;

	/** 最新更新日時 */
	private Long m_updateTime;

	/** 有効/無効 */
	private Boolean m_valid = false;

	/** カレンダID */
	private String m_calendarId;

	/** カレンダ(エージェントに渡すとき以外はnull) */
	private CalendarInfo m_calendarInfo;

	/** ランタイムジョブ変数情報 */
	private ArrayList<JobRuntimeParam> m_jobRuntimeParamList;

	public JobKick() {
		this.m_type = JobKickConstant.TYPE_MANUAL;
	}

	/**
	 * 実行契機種別を返す<BR>
	 * @return JobKickConstant.TYPE_****
	 */
	public int getType() {
		return m_type;
	}
	/**
	 * 実行契機種別を設定する<BR>
	 * @param type
	 */
	public void setType(int type) {
		this.m_type = type;
	}

	/**
	 * 実行契機IDを返す<BR>
	 * @return スケジュールID
	 */
	public String getId() {
		return m_id;
	}

	/**
	 * 実行契機IDを設定する<BR>
	 * @param id スケジュールID
	 */
	public void setId(String id) {
		this.m_id = id;
	}

	/**
	 * ジョブIDを返す<BR>
	 * @return ジョブID
	 */
	public String getJobId() {
		return m_jobId;
	}

	/**
	 * ジョブIDを設定する<BR>
	 * @param jobId ジョブID
	 */
	public void setJobId(String jobId) {
		this.m_jobId = jobId;
	}

	/**
	 * ジョブ名を返す<BR>
	 * @return ジョブ名
	 */
	public String getJobName() {
		return m_jobName;
	}

	/**
	 * ジョブ名を設定する<BR>
	 * @param jobName ジョブ名
	 */
	public void setJobName(String jobName) {
		this.m_jobName = jobName;
	}

	/**
	 * 実行契機名を返す<BR>
	 * @return スケジュール名
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * 実行契機名を設定する<BR>
	 * @param name スケジュール名
	 */
	public void setName(String name) {
		this.m_name = name;
	}

	/**
	 * 有効/無効を返す<BR>
	 * @return 有効/無効
	 */
	public Boolean isValid() {
		return m_valid;
	}

	/**
	 * 有効/無効を設定する<BR>
	 * @param valid 有効/無効
	 */
	public void setValid(Boolean valid) {
		this.m_valid = valid;
	}

	/**
	 * カレンダIDを返す<BR>
	 * @return カレンダID
	 */
	public String getCalendarId() {
		return m_calendarId;
	}

	/**
	 * カレンダIDを設定する<BR>
	 * @param calendarId カレンダID
	 */
	public void setCalendarId(String calendarId) {
		this.m_calendarId = calendarId;
	}

	/**
	 * カレンダを返す<BR>
	 * @return カレンダ
	 */
	public CalendarInfo getCalendarInfo() {
		return m_calendarInfo;
	}

	/**
	 * カレンダを設定する<BR>
	 * @param calendar カレンダ
	 */
	public void setCalendarInfo(CalendarInfo calendarInfo) {
		this.m_calendarInfo = calendarInfo;
	}

	/**
	 * 作成日時を返す<BR>
	 * @return 作成日時
	 */
	public Long getCreateTime() {
		return m_createTime;
	}

	/**
	 * 作成日時を設定する<BR>
	 * @param createTime 作成日時
	 */
	public void setCreateTime(Long createTime) {
		this.m_createTime = createTime;
	}
	/**
	 * 最新更新日時を返す<BR>
	 * @return 最新更新日時
	 */
	public Long getUpdateTime() {
		return m_updateTime;
	}

	/**
	 * 最新更新日時を設定する<BR>
	 * @param updateTime 最新更新日時
	 */
	public void setUpdateTime(Long updateTime) {
		this.m_updateTime = updateTime;
	}

	/**
	 * 新規作成ユーザを返す<BR>
	 * @return 新規作成ユーザ
	 */
	public String getCreateUser() {
		return m_createUser;
	}

	/**
	 * 新規作成ユーザを設定する<BR>
	 * @param createUser 新規作成ユーザ
	 */
	public void setCreateUser(String createUser) {
		this.m_createUser = createUser;
	}

	/**
	 * 最新更新ユーザを返す<BR>
	 * @return 最新更新ユーザ
	 */
	public String getUpdateUser() {
		return m_updateUser;
	}

	/**
	 * 最新更新ユーザを設定する<BR>
	 * @param updateUser 最新更新ユーザ
	 */
	public void setUpdateUser(String updateUser) {
		this.m_updateUser = updateUser;
	}

	/**
	 * 所属ジョブユニットのジョブIDを返す<BR>
	 * @return 所属ジョブユニットのジョブID
	 */
	public String getJobunitId() {
		return m_jobunitId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを設定する<BR>
	 * @param jobunitId 所属ジョブユニットのジョブID
	 */
	public void setJobunitId(String jobunitId) {
		m_jobunitId = jobunitId;
	}

	/**
	 * オーナーロールIDを返す<BR>
	 * @return オーナーロールID
	 */
	public String getOwnerRoleId() {
		return m_ownerRoleId;
	}

	/**
	 * オーナーロールIDを設定する<BR>
	 * @param ownerRoleId オーナーロールID
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
	}

	public ArrayList<JobRuntimeParam> getJobRuntimeParamList() {
		return m_jobRuntimeParamList;
	}

	public void setJobRuntimeParamList(ArrayList<JobRuntimeParam> jobRuntimeParamList) {
		this.m_jobRuntimeParamList = jobRuntimeParamList;
	}
}