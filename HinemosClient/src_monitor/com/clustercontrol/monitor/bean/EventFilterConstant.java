/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

/**
 * 
 * EventFilterProperty.javaの定数部分を切り出した物。
 *
 */
public class EventFilterConstant {
	
	/** マネージャ **/
	public static final String MANAGER_NAME = "managerName";
	
	/**
	 * SQL検索の有無
	 */
	public static final String ALL_SEARCH = "allSearch";

	/**
	 * 重要度。
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public static final String PRIORITY = "priority";
	/** 重要度（危険）。 */
	public static final String PRIORITY_CRITICAL = "priorityCritical";
	/** 重要度（警告）。 */
	public static final String PRIORITY_WARNING = "priorityWarning";
	/** 重要度（情報）。 */
	public static final String PRIORITY_INFO = "priorityInfo";
	/** 重要度（不明）。 */
	public static final String PRIORITY_UNKNOWN = "priorityUnknown";

	/** 受信日時（開始）。 */
	public static final String OUTPUT_FROM_DATE = "outputFromDate";
	/** 受信日時（終了）。 */
	public static final String OUTPUT_TO_DATE = "outputToDate";
	/** 出力日時（開始）。 */
	public static final String GENERATION_FROM_DATE = "generationFromDate";
	/** 出力日時（終了）。 */
	public static final String GENERATION_TO_DATE = "generationToDate";
	
	/** 監視項目ID。 */
	public static final String MONITOR_ID = "monitorId";
	/** 監視詳細。 */
	public static final String MONITOR_DETAIL_ID = "monitorDetailId";
	
	/**
	 * ファシリティのターゲット 。
	 * @see com.clustercontrol.repository.bean.FacilityTargetConstant
	 */
	public static final String FACILITY_TYPE = "facilityType";
	/** アプリケーション。 */
	public static final String APPLICATION = "application";
	/** メッセージ。 */
	public static final String MESSAGE = "message";
	/** 確認 */
	public static final String CONFIRMED = "confirmed";
	/** 確認（未確認）。 */
	public static final String CONFIRMED_UNCONFIRMED = "confirmedUnconfirmed";
	/** 確認（確認中）。 */
	public static final String CONFIRMED_CONFIRMING = "confirmedConfirming";
	/** 確認（確認済）。 */
	public static final String CONFIRMED_CONFIRMED = "confirmedConfirmed";

	/** 受信日時。 */
	public static final String OUTPUT_DATE = "outputDate";
	/** 出力日時。 */
	public static final String GENERATION_DATE = "generationDate";
	/** 確認ユーザ */
	public static final String CONFIRM_USER = "confirmUser";
	/** コメント。 */
	public static final String COMMENT = "comment";
	/** コメント入力日時。 */
	public static final String COMMENT_DATE = "commentDate";
	/** コメント入力ユーザ。 */
	public static final String COMMENT_USER = "commentUser";
	/** オーナーロールID。 */
	public static final String OWNER_ROLE_ID = "ownerRoleId";
	/** 性能グラフ用フラグ */
	public static final String COLLECT_GRAPH_FLG = "collectGraphFlg";
	/** ユーザ項目フォーマット */
	private static final String USER_ITEM_FORMAT = "userItem%02d";
	/** イベント番号(自) */
	public static final String EVENT_NO_FROM = "eventNoFrom";
	/** イベント番号(至) */
	public static final String EVENT_NO_TO = "eventNoTo";
	
	public static String getUserItemConst(int index) {
		return String.format(USER_ITEM_FORMAT, index);
	}
}
