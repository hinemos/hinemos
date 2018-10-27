/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

/**
 * 監視[一覧]ビューのフィルタ設定の定数部分を切り出したるクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class MonitorFilterConstant {

	/** マネージャ */
	public static final String MANAGER = "manager";

	/** 監視項目ID */
	public static final String MONITOR_ID = "monitorId";

	/** 監視種別ID(プラグインID) */
	public static final String MONITOR_TYPE_ID = "monitorTypeId";

	/** 説明 */
	public static final String DESCRIPTION = "description";

	/** ファシリティID */
	public static final String FACILITY_ID = "facilityId";

	/** カレンダ */
	public static final String CALENDAR_ID = "calendarId";

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

	/** 監視有効フラグ */
	public static final String MONITOR_FLG = "monitorFlg";

	/** 収集有効フラグ */
	public static final String COLLECTOR_FLG = "collectorFlg";

	/** オーナーロールID */
	public static final String OWNER_ROLE_ID = "ownerRoleId";
}
