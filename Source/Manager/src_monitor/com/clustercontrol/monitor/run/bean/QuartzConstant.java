/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
