/*

Copyright (C) since 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
