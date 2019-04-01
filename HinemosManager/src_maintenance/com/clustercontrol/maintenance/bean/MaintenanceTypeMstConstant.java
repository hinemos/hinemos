/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.bean;



/**
 * メンテナンス機能の種別の定義を定数として格納するクラスです。
 *
 * @version 6.1.0 メンテナンス対象にバイナリ収集データ削除追加
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

	public static final String DELETE_COLLECT_BINFILE_DATA = "DELETE_COLLECT_BINFILE_DATA";
	public static final String DELETE_COLLECT_PCAP_DATA = "DELETE_COLLECT_PCAP_DATA";

	public static final String DELETE_NODE_CONFIG_HISTORY = "DELETE_NODE_CONFIG_HISTORY";
}
