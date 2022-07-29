/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブ[履歴]ビューのフィルタ用文字列定義を定数として定義するクラス<BR
 *
 * @version 2.4.0
 * @since 1.0.0
 */
public class HistoryFilterPropertyConstant {
	/** マネージャ */
	public static final String MANAGER = "manager";

	/** 開始・再実行日時（開始） */
	public static final String START_FROM_DATE = "startFromDate";

	/** 開始・再実行日時（終了） */
	public static final String START_TO_DATE = "startToDate";

	/** 終了・中断日時（開始） */
	public static final String END_FROM_DATE = "endFromDate";

	/** 終了・中断日時（終了） */
	public static final String END_TO_DATE = "endToDate";

	/** セッションID */
	public static final String SESSION_ID = "sessionId";

	/** ジョブID */
	public static final String JOB_ID = "jobId";

	/** 実行状態 */
	public static final String STATUS = "status";

	/** 終了状態 */
	public static final String END_STATUS = "endStatus";

	/** 実行契機種別 */
	public static final String TRIGGER_TYPE = "triggerType";

	/** 実行契機情報 */
	public static final String TRIGGER_INFO = "triggerInfo";

	/** 開始・再実行日時 */
	public static final String START_DATE = "startDate";

	/** 終了・中断日時 */
	public static final String END_DATE = "endDate";

	/** 実行契機 */
	public static final String TRIGGER = "trigger";

	/** オーナーロールID */
	public static final String OWNER_ROLE_ID = "ownerRoleId";
}