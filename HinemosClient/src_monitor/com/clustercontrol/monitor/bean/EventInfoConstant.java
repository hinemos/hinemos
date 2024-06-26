/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

/**
 * EventInfoPropertyの定数部分を切り出した物。
 *
 */
public class EventInfoConstant {
	/**
	 * 重要度。
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public static final String PRIORITY = "priority";
	/** 受信日時。 */
	public static final String OUTPUT_DATE = "outputDate";
	/** 出力日時。 */
	public static final String GENERATION_DATE = "generationDate";
	/** プラグインID。 */
	public static final String PLUGIN_ID = "pluginId";
	/** 監視項目ID。 */
	public static final String MONITOR_ID = "monitorId";
	/** 監視詳細。 */
	public static final String MONITOR_DETAIL_ID = "monitorDetailId";
	/** ファシリティID。 */
	public static final String FACILITY_ID = "facilityId";
	/** スコープ。 */
	public static final String SCOPE_TEXT = "scopeText";
	/** アプリケーション。 */
	public static final String APPLICATION = "application";
	/** メッセージ。 */
	public static final String MESSAGE = "message";
	/** オリジナルメッセージ。 */
	public static final String MESSAGE_ORG = "messageOrg";
	/**
	 * 確認。
	 * @see com.clustercontrol.monitor.bean.ConfirmConstant
	 */
	public static final String CONFIRMED = "confirmed";
	/** 確認日時。 */
	public static final String CONFIRM_DATE = "confirmDate";
	/** 確認ユーザ */
	public static final String CONFIRM_USER = "confirmUser";
	/** 重複カウント。 */
	public static final String DUPLICATION_COUNT = "duplicationCount";
	/** コメント。 */
	public static final String COMMENT = "comment";
	/** コメント更新日時 。*/
	public static final String COMMENT_DATE = "commentDate";
	/** コメント更新ユーザ 。*/
	public static final String COMMENT_USER = "commentUser";
	/** 性能グラフ用フラグ */
	public static final String COLLECT_GRAPH_FLG = "collectGraphFlg";
	/** オーナーロールID */
	public static final String OWNER_ROLE_ID = "ownerRoleId";
	/** イベント操作履歴 */
	public static final String EVENT_HISTORY = "eventHistory";
	/** ユーザ項目フォーマット */
	private static final String USER_ITEM_FORMAT = "userItem%02d";
	/** イベント番号 */
	public static final String EVENT_NO = "eventNo";
	/** イベント番号(自) */
	public static final String EVENT_NO_FROM = "eventNoFrom";
	/** イベント番号(至) */
	public static final String EVENT_NO_TO = "eventNoTo";
	
	public static String getUserItemConst(int index) {
		return String.format(USER_ITEM_FORMAT, index);
	}
	/** 通知のUUID */
	public static final String NOTIFY_UUID = "notifyUUID";
}
