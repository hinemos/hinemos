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
 * StatusInfoPropertyの定数部分を切り出した物。
 *
 */
public class StatusInfoConstant {

	/** マネージャ名 */
	public static final String MANAGER_NAME = "managerName";
	/**
	 * 重要度。
	 * @see com.clustercontrol.bean.PriorityConstant
	 */
	public static final String PRIORITY = "priority";
	/** プラグインID */
	public static final String PLUGIN_ID = "pluginId";
	/** 監視項目ID。 */
	public static final String MONITOR_ID = "monitorId";
	/** 監視詳細。 */
	public static final String MONITOR_DETAIL_ID = "monitorDetail";
	/** ファシリティID。 */
	public static final String FACILITY_ID = "facilityId";
	/** スコープ。 */
	public static final String SCOPE_TEXT = "scope";
	/** アプリケーション。 */
	public static final String APPLICATION = "application";
	/** 最終変更日時。 */
	public static final String UPDATE_TIME = "updateTime";
	/** 出力日時。 */
	public static final String OUTPUT_TIME = "outputTime";
	/** メッセージ。 */
	public static final String MESSAGE = "message";
	/** オーナーロールID。 */
	public static final String OWNER_ROLE_ID = "ownerRoleId";

}
