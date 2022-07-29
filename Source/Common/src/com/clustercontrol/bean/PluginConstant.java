/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

/**
 * Hinemosの機能を定数として格納するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.1.2
 */
public class PluginConstant {
	/** リポジトリ */
	public static final int TYPE_REPOSITORY = 0;
	/** アクセス */
	public static final int TYPE_ACCESSCONTROL = 1;
	/** ジョブ管理 */
	public static final int TYPE_JOBMANAGEMENT = 2;
	/** 性能管理 */
	public static final int TYPE_PERFORMANCE = 4;
	/** 監視設定 */
	public static final int TYPE_MONITOR = 5;
	/** システムログ監視 */
	public static final int TYPE_SYSTEMLOG_MONITOR = 6;
	/** Hinemosエージェント監視 */
	public static final int TYPE_AGENT_MONITOR = 7;
	/** HTTP監視 */
	public static final int TYPE_HTTP_MONITOR = 8;
	/** プロセス監視 */
	public static final int TYPE_PROCESS_MONITOR = 9;
	/** SQL監視 */
	public static final int TYPE_SQL_MONITOR = 10;
	/** SNMP監視 */
	public static final int TYPE_SNMP_MONITOR = 11;
	/** PING監視 */
	public static final int TYPE_PING_MONITOR = 12;
	/** カレンダ */
	public static final int TYPE_CALENDAR = 13;
	/** 通知 */
	public static final int TYPE_NOTIFY = 14;
	/** 重要度判定 */
	public static final int TYPE_PRIORITY_JUDGMENT = 15;
	/** ログ転送 */
	public static final int TYPE_LOG_TRANSFER = 16;
	/** 障害検知 */
	public static final int TYPE_TROUBLE_DETECTION = 17;
	/** SNMPTRAP監視 */
	public static final int TYPE_SNMPTRAP_MONITOR = 18;
	/** リソース監視 */
	public static final int TYPE_PERFORMANCE_MONITOR = 19;
	/** サービス・ポート監視 */
	public static final int TYPE_PORT_MONITOR = 20;
	/** カスタム監視 */
	public static final int TYPE_CUSTOM_MONITOR = 21;
	/** Windowsサービス監視 */
	public static final int TYPE_WINSERVICE_MONITOR = 22;
	/** Windowsイベント監視 */
	public static final int TYPE_WINEVENT_MONITOR = 23;
	/** 環境構築 */
	public static final int TYPE_INFRA = 24;
	/** カスタムトラップ監視 */
	public static final int TYPE_CUSTOMTRAP_MONITOR = 25;
}
