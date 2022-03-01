/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブ履歴[受信ジョブ連携メッセージ一覧]ビューのフィルタ用文字列定義を定数として定義するクラス<BR
 *
 */
public class JobLinkMessageFilterPropertyConstant {
	/** マネージャ */
	public static final String MANAGER = "manager";

	/** ジョブ連携メッセージID */
	public static final String JOBLINK_MESSAGE_ID = "joblinkMessageId";

	/** 送信元ファシリティID */
	public static final String SRC_FACILITY_ID = "srcFacilityId";

	/** 送信元ファシリティ名 */
	public static final String SRC_FACILITY_NAME = "srcFacilityName";

	/** 監視詳細 */
	public static final String MONITOR_DETAIL_ID = "monitorDetailId";

	/** アプリケーション */
	public static final String APPLICATION = "application";

	/** 重要度 */
	public static final String PRIORITY = "priority";

	/** 重要度（危険） */
	public static final String PRIORITY_CRITICAL = "priorityCritical";

	/** 重要度（警告） */
	public static final String PRIORITY_WARNING = "priorityWarning";

	/** 重要度（情報） */
	public static final String PRIORITY_INFO = "priorityInfo";

	/** 重要度（不明） */
	public static final String PRIORITY_UNKNOWN = "priorityUnknown";

	/** メッセージ */
	public static final String MESSAGE = "message";

	/** 送信日時 */
	public static final String SEND_DATE = "sendDate";

	/** 送信日時（開始） */
	public static final String SEND_FROM_DATE = "sendFromDate";

	/** 送信日時（終了） */
	public static final String SEND_TO_DATE = "sendToDate";

	/** 受信日時 */
	public static final String ACCEPT_DATE = "acceptDate";

	/** 受信日時（開始） */
	public static final String ACCEPT_FROM_DATE = "acceptFromDate";

	/** 受信日時（終了） */
	public static final String ACCEPT_TO_DATE = "acceptToDate";
}