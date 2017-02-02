/*

Copyright (C) since 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.jobmanagement.bean;

/**
 * 
 * JobKickDataPropertyの定数部分を切り出したもの。
 *
 */
public class JobKickFilterConstant {
	/** マネージャ */
	public static final String MANAGER = "manager";
	/** 実行契機ID */
	public static final String JOBKICK_ID = "jobkickId";
	/** 実行契機名 */
	public static final String JOBKICK_NAME = "jobkickName";
	/** 実行契機種別 */
	public static final String JOBKICK_TYPE = "jobkickType";
	/** ジョブユニットID */
	public static final String JOBUNIT_ID = "jobunitId";
	/** ジョブID */
	public static final String JOB_ID = "jobId";
	/** カレンダID */
	public static final String CALENDAR_ID = "calendarId";
	/** 有効フラグ */
	public static final String VALID_FLG = "validFlg";
	/** オーナーロールID */
	public static final String OWNER_ROLE_ID = "ownerRoleId";
	/** 新規作成者 */
	public static final String REG_USER = "regUser";
	/** 作成日時 */
	public static final String REG_DATE = "regDate";
	/** 作成日時(FROM) */
	public static final String REG_FROM_DATE = "regFromDate";
	/** 作成日時(TO) */
	public static final String REG_TO_DATE = "regToDate";
	/** 最終変更者 */
	public static final String UPDATE_USER = "updateUser";
	/** 最終変更日時 */
	public static final String UPDATE_DATE = "updateDate";
	/** 最終変更日時(FROM) */
	public static final String UPDATE_FROM_DATE = "updateFromDate";
	/** 最終変更日時(TO) */
	public static final String UPDATE_TO_DATE = "updateToDate";
}
