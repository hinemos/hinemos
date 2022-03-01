/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
	protected int type;

	/** 実行契機ID */
	private String id;

	/** 実行契機名 */
	private String name;

	/** ジョブID */
	private String jobId;

	/** ジョブ名 */
	private String jobName;

	/** ジョブユニットID */
	private String jobunitId;

	/** オーナーロールID */
	private String ownerRoleId;

	/** 新規作成ユーザ */
	private String createUser;

	/** 作成日時 */
	private Long createTime;

	/** 最新更新ユーザ */
	private String updateUser;

	/** 最新更新日時 */
	private Long updateTime;

	/** 有効/無効 */
	private Boolean valid = false;

	/** カレンダID */
	private String calendarId;

	/** カレンダ(エージェントに渡すとき以外はnull) */
	private CalendarInfo calendarInfo;

	/** ランタイムジョブ変数情報 */
	private ArrayList<JobRuntimeParam> jobRuntimeParamList;

	public JobKick() {
		this.type = JobKickConstant.TYPE_MANUAL;
	}

	/**
	 * 実行契機種別を返す<BR>
	 * @return JobKickConstant.TYPE_****
	 */
	public int getType() {
		return type;
	}
	/**
	 * 実行契機種別を設定する<BR>
	 * @param type
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * 実行契機IDを返す<BR>
	 * @return スケジュールID
	 */
	public String getId() {
		return id;
	}

	/**
	 * 実行契機IDを設定する<BR>
	 * @param id スケジュールID
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * ジョブIDを返す<BR>
	 * @return ジョブID
	 */
	public String getJobId() {
		return jobId;
	}

	/**
	 * ジョブIDを設定する<BR>
	 * @param jobId ジョブID
	 */
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	/**
	 * ジョブ名を返す<BR>
	 * @return ジョブ名
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * ジョブ名を設定する<BR>
	 * @param jobName ジョブ名
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * 実行契機名を返す<BR>
	 * @return スケジュール名
	 */
	public String getName() {
		return name;
	}

	/**
	 * 実行契機名を設定する<BR>
	 * @param name スケジュール名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 有効/無効を返す<BR>
	 * @return 有効/無効
	 */
	public Boolean isValid() {
		return valid;
	}

	/**
	 * 有効/無効を設定する<BR>
	 * @param valid 有効/無効
	 */
	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	/**
	 * カレンダIDを返す<BR>
	 * @return カレンダID
	 */
	public String getCalendarId() {
		return calendarId;
	}

	/**
	 * カレンダIDを設定する<BR>
	 * @param calendarId カレンダID
	 */
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	/**
	 * カレンダを返す<BR>
	 * @return カレンダ
	 */
	public CalendarInfo getCalendarInfo() {
		return calendarInfo;
	}

	/**
	 * カレンダを設定する<BR>
	 * @param calendar カレンダ
	 */
	public void setCalendarInfo(CalendarInfo calendarInfo) {
		this.calendarInfo = calendarInfo;
	}

	/**
	 * 作成日時を返す<BR>
	 * @return 作成日時
	 */
	public Long getCreateTime() {
		return createTime;
	}

	/**
	 * 作成日時を設定する<BR>
	 * @param createTime 作成日時
	 */
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	/**
	 * 最新更新日時を返す<BR>
	 * @return 最新更新日時
	 */
	public Long getUpdateTime() {
		return updateTime;
	}

	/**
	 * 最新更新日時を設定する<BR>
	 * @param updateTime 最新更新日時
	 */
	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * 新規作成ユーザを返す<BR>
	 * @return 新規作成ユーザ
	 */
	public String getCreateUser() {
		return createUser;
	}

	/**
	 * 新規作成ユーザを設定する<BR>
	 * @param createUser 新規作成ユーザ
	 */
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	/**
	 * 最新更新ユーザを返す<BR>
	 * @return 最新更新ユーザ
	 */
	public String getUpdateUser() {
		return updateUser;
	}

	/**
	 * 最新更新ユーザを設定する<BR>
	 * @param updateUser 最新更新ユーザ
	 */
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	/**
	 * 所属ジョブユニットのジョブIDを返す<BR>
	 * @return 所属ジョブユニットのジョブID
	 */
	public String getJobunitId() {
		return this.jobunitId;
	}

	/**
	 * 所属ジョブユニットのジョブIDを設定する<BR>
	 * @param jobunitId 所属ジョブユニットのジョブID
	 */
	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	/**
	 * オーナーロールIDを返す<BR>
	 * @return オーナーロールID
	 */
	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	/**
	 * オーナーロールIDを設定する<BR>
	 * @param ownerRoleId オーナーロールID
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public ArrayList<JobRuntimeParam> getJobRuntimeParamList() {
		return jobRuntimeParamList;
	}

	public void setJobRuntimeParamList(ArrayList<JobRuntimeParam> jobRuntimeParamList) {
		this.jobRuntimeParamList = jobRuntimeParamList;
	}
}