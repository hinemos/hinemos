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

package com.clustercontrol.monitor.run.bean;

/**
 * Quartz関連の定義を定数として格納するクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class QuartzConstant {

	/**
	 * Quartzから呼び出すメソッド名。<BR>
	 * 監視の実行処理を行うメソッドの名前です。
	 */
	public static final String MONITOR_METHOD_MONITOR_AGGREGATED = "runMonitorAggregatedMonitorId";
	public static final String MONITOR_METHOD_FACILITY_AGGREGATED = "runMonitorAggregatedFacilityId";
	
	
	/**
	 * JobDetailの監視情報の有効／無効のキー名。
	 */
	public static final String VALID_KEY = "valid";

	public static final String QUARTZ_NAME = "DBMSScheduler";
}
