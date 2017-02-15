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

package com.clustercontrol.maintenance.bean;



/**
 * メンテナンス機能の種別の定義を定数として格納するクラスです。
 *
 * @version 4.0.0
 * @since 2.2.0
 */
public class MaintenanceTypeMstConstant {
	//メンテナンス種別カラム順
	public static final int TYPE_ID = 0;
	public static final int NAME_ID = 1;
	public static final int ORDER_NO = 2;

	//メンテナンス種別ID
	public static final String DELETE_EVENT_LOG_ALL = "DELETE_EVENT_LOG_ALL";
	public static final String DELETE_EVENT_LOG = "DELETE_EVENT_LOG";

	public static final String DELETE_JOB_HISTORY_ALL = "DELETE_JOB_HISTORY_ALL";
	public static final String DELETE_JOB_HISTORY = "DELETE_JOB_HISTORY";

	public static final String DELETE_COLLECT_DATA_RAW = "DELETE_COLLECT_DATA_RAW";	
	public static final String DELETE_SUMMARY_DATA_HOUR = "DELETE_SUMMARY_DATA_HOUR";
	public static final String DELETE_SUMMARY_DATA_DAY = "DELETE_SUMMARY_DATA_DAY";
	public static final String DELETE_SUMMARY_DATA_MONTH = "DELETE_SUMMARY_DATA_MONTH";

	public static final String DELETE_COLLECT_STRING_DATA = "DELETE_COLLECT_STRING_DATA";
}
