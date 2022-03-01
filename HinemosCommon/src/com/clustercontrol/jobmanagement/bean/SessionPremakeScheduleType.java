/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;


/**
 * ジョブセッション事前生成スケジュール種別の定数を定義するクラス<BR>
 * 
 */
public class SessionPremakeScheduleType {

	/** 毎日 */
	public static final int TYPE_EVERY_DAY = 0;

	/** 毎週 */
	public static final int TYPE_EVERY_WEEK = 1;

	/** 時間 */
	public static final int TYPE_TIME = 2;

	/** 日時 */
	public static final int TYPE_DATETIME = 3;

	/** 毎日(Enum) */
	public static final String ENUM_NAME_EVERY_DAY = "EVERY_DAY";

	/** 毎週(Enum) */
	public static final String ENUM_NAME_EVERY_WEEK = "EVERY_WEEK";

	/** 時間(Enum) */
	public static final String ENUM_NAME_TIME = "TIME";

	/** 日時(Enum) */
	public static final String ENUM_NAME_DATETIME = "DATETIME";
}