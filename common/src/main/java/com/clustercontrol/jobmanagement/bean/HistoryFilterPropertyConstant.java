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