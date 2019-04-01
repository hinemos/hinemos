/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.constant;

import com.clustercontrol.util.Messages;

/**
 * Hinemosの機能を定数として格納するクラス<BR>
 *
 * @version 6.0.0
 * @since 1.2.0
 * 
 */
public class HinemosModuleConstant {
	
	/** 共通プラットフォーム*/
	public static final String PLATFORM  = "PLT";
	/** 通知 */
	public static final String PLATFORM_NOTIFY = "PLT_NTF";
	/** メールテンプレート*/
	public static final String PLATFORM_MAIL_TEMPLATE = "PLT_MIL_TMP";
	/** ログフォーマット*/
	public static final String PLATFORM_LOG_FORMAT = "HUB_LF";
	/** 転送設定 */
	public static final String PLATFORM_LOG_TRANSFER = "HUB_TRF";
	/** カレンダ */
	public static final String PLATFORM_CALENDAR = "PLT_CAL";
	/** カレンダパターン */
	public static final String PLATFORM_CALENDAR_PATTERN = "PLT_CAL_PTN";
	/** アクセス */
	public static final String PLATFORM_ACCESS_USER_ROLE = "PLT_ACC_USER_ROLE";
	/** アクセス */
	public static final String PLATFORM_ACCESS_SYSTEM_PRIVILEGE = "PLT_ACC_SYSTEM_PRIVILEGE";
	/** アクセス */
	public static final String PLATFORM_ACCESS_OBJECT_PRIVILEGE = "PLT_ACC_OBJECT_PRIVILEGE";
	/** 重要度判定 */
	public static final String PLATFORM_PRIORITY_JUDGMENT = "PLT_PRI_JMT";
	/** リポジトリ */
	public static final String PLATFORM_REPOSITORY = "PLT_REP";
	/** リポジトリ */
	public static final String PLATFORM_REPOSITORY_NODE = "PLT_REP_NOD";
	/** リポジトリ */
	public static final String PLATFORM_REPOSITORY_SCOPE = "PLT_REP_SCP";
	
	/** Hinemosプロパティ */
	public static final String PLATFORM_HINEMOS_PROPERTY = "PLT_HIN_PROP";
	
	/** HTTP監視 */
	public static final String MONITOR_HTTP = "MON_HTP";
	/** SNMP監視 */
	public static final String MONITOR_SNMP = "MON_SNMP";
	/** SNMPTRAP監視 */
	public static final String MONITOR_SNMPTRAP = "MON_SNMP_TRP";
	/** SQL監視 */
	public static final String MONITOR_SQL = "MON_SQL";
	/** コマンド監視 */
	public static final String MONITOR_CUSTOM = "MON_CUSTOM";
	/** カスタムトラップ監視 */
	public static final String MONITOR_CUSTOMTRAP = "MON_CUSTOMTRAP";
	
	/** 性能管理 */
	public static final String PERFORMANCE_RECORD = "PRF_REC";
	
	/** ジョブ管理 */
	public static final String JOB = "JOB";
	/** ジョブ管理 */
	public static final String JOB_MST  = "JOB_MST";

	/** ジョブ管理 */
	public static final String JOB_KICK  = "JOB_KICK";
	/** ジョブ管理 */
	public static final String JOBMAP_IMAGE_FILE  = "JOBMAP_IMAGE_FILE";

	/** メンテナンス */
	public static final String SYSYTEM_MAINTENANCE = "MAINTENANCE";
	
	/** マスタ */
	public static final String MASTER = "MST";
	/** プラットフォームマスタ */
	public static final String MASTER_PLATFORM = "MST_PLT";
	/** 収集項目マスタ */
	public static final String MASTER_COLLECT = "MST_COL";
	
	/** JMXマスタ */
	public static final String MASTER_JMX = "MST_JMX";

	/** レポーティング */
	public static final String REPORTING = "REPORTING"; 
	
	/** 設定 */
	public static final String STRING_SETTING = Messages.getString("setting");
	/** マスタ */
	public static final String STRING_MASTER = Messages.getString("master");
	
	/** 共通 */
	public static final String STRING_PLATFORM = Messages.getString("platform");

	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_SCOPE = Messages.getString("platform.repository.scope");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_SCOPE_NODE = Messages.getString("platform.repository.scope.node");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE = Messages.getString("platform.repository.node");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_HOSTNAME = Messages.getString("platform.repository.hostname");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_CPU = Messages.getString("platform.repository.cpu");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_MEMORY = Messages.getString("platform.repository.memory");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_NETWORKINTERFACE = Messages.getString("platform.repository.networkinterface");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_DISK = Messages.getString("platform.repository.disk");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_FS = Messages.getString("platform.repository.fs");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_DEVICE = Messages.getString("platform.repository.device");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_NETSTAT = Messages.getString("platform.repository.netstat");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_PROCESS = Messages.getString("platform.repository.process");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_PACKAGE = Messages.getString("platform.repository.package");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_PRODUCT = Messages.getString("platform.repository.product");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_LICENSE = Messages.getString("platform.repository.license");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_VARIABLE = Messages.getString("platform.repository.variable");
	/** リポジトリ */
	public static final String STRING_PLATFORM_REPOSITORY_NODE_NOTE = Messages.getString("platform.repository.note");

	/** メール */
	public static final String STRING_PLATFORM_MAIL_TEMPLATE = Messages.getString("platform.mailtemplate");

	/** ログフォーマット */
	public static final String STRING_PLATFORM_LOG_FORMAT = Messages.getString("platform.logformat");

	/** アクセス */
	public static final String STRING_PLATFORM_ACCESS_USER_ROLE = Messages.getString("platform.accesscontrol.user.role");
	/** アクセス */
	public static final String STRING_PLATFORM_ACCESS_USER = Messages.getString("platform.accesscontrol.user");
	/** アクセス */
	public static final String STRING_PLATFORM_ACCESS_ROLE = Messages.getString("platform.accesscontrol.role");
	/** アクセス */
	public static final String STRING_PLATFORM_ACCESS_ROLE_USER = Messages.getString("platform.accesscontrol.role.user");
	/** アクセス */
	public static final String STRING_PLATFORM_ACCESS_SYSTEM_PRIVILEGE = Messages.getString("platform.accesscontrol.system.privilege");
	/** アクセス */
	public static final String STRING_PLATFORM_ACCESS_OBJECT_PRIVILEGE = Messages.getString("platform.accesscontrol.object.privilege");
	/** カレンダ */
	public static final String STRING_PLATFORM_CALENDAR = Messages.getString("platform.calendar");
	/** カレンダ */
	public static final String STRING_PLATFORM_CALENDAR_PATTERN = Messages.getString("platform.calendar.pattern");
	/** 通知 */
	public static final String STRING_PLATFORM_NOTIFY = Messages.getString("platform.notify");

	/** 監視管理 */
	public static final String STRING_MONITOR = Messages.getString("monitorsetting");
	/** Hinemosエージェント監視 */
	public static final String STRING_MONITOR_AGENT = Messages.getString("monitor.agent");
	/** HTTP監視 */
	public static final String STRING_MONITOR_HTTP = Messages.getString("monitor.http");
	/** リソース監視 */
	public static final String STRING_MONITOR_PERFORMANCE = Messages.getString("monitor.perf");
	/** PING監視 */
	public static final String STRING_MONITOR_PING = Messages.getString("monitor.ping");
	/** サービス・ポート監視 */
	public static final String STRING_MONITOR_PORT = Messages.getString("monitor.port");
	/** プロセス監視 */
	public static final String STRING_MONITOR_PROCESS = Messages.getString("monitor.process");
	/** SNMP監視 */
	public static final String STRING_MONITOR_SNMP = Messages.getString("monitor.snmp");
	/** SNMPTRAP監視 */
	public static final String STRING_MONITOR_SNMPTRAP = Messages.getString("monitor.snmptrap");
	/** SQL監視 */
	public static final String STRING_MONITOR_SQL = Messages.getString("monitor.sql");
	/** コマンド監視 */
	public static final String STRING_MONITOR_CUSTOM = Messages.getString("monitor.custom");
	/** システムログ監視 */
	public static final String STRING_MONITOR_SYSTEMLOG = Messages.getString("monitor.systemlog");
	/** ログファイル監視 */
	public static final String STRING_MONITOR_LOGFILE = Messages.getString("monitor.logfile");
	/** Windows サービス監視 */
	public static final String STRING_MONITOR_WINSERVICE = Messages.getString("monitor.winservice");
	/** Windowsイベント監視 */
	public static final String STRING_MONITOR_WINEVENT = Messages.getString("monitor.winevent");
	/** カスタムトラップ監視 */
	public static final String STRING_MONITOR_CUSTOMTRAP = Messages.getString("monitor.customtrap");

	/** HTTP監視（シナリオ） */
	public static final String STRING_MONITOR_HTTP_SCENARIO = Messages.getString("monitor.http.scenario");
	/** JMX監視 */
	public static final String STRING_MONITOR_JMX = Messages.getString("monitor.jmx");
	
	/** ジョブ管理 */
	public static final String STRING_JOB = Messages.getString("job.management");
	/** ジョブ管理 */
	public static final String STRING_JOB_MST = Messages.getString("job.management.master");
	/** ジョブ管理 */
	public static final String STRING_JOB_KICK= Messages.getString("job.management.kick");
	/** ジョブ管理 */
	public static final String STRING_JOB_SCHEDULE= Messages.getString("job.management.schedule");
	/** ジョブ管理 */
	public static final String STRING_JOB_FILECHECK= Messages.getString("job.management.filecheck");
	/** ジョブ管理 */
	public static final String STRING_JOB_MANUAL= Messages.getString("job.management.manual");
	
	/** メンテナンス */
	public static final String STRING_SYSYTEM_MAINTENANCE = Messages.getString("system.maintenance");
	
	/** プラットフォームマスタ */
	public static final String STRING_MASTER_PLATFORM = Messages.getString("master.platform");
	/** 収集項目マスタ */
	public static final String STRING_MASTER_COLLECT = Messages.getString("master.collect");
	
	/** JMXマスタ */
	public static final String STRING_MASTER_JMX = Messages.getString("master.jmx");
	
	/** Hinemosプロパティ */
	public static final String STRING_PLATFORM_HINEMOS_PROPERTY = Messages.getString("platform.hinemos.property");
	
	/** 収集蓄積 */
	public static final String HUB = "HUB";
	/** 収集蓄積 */
	public static final String STRING_HUB = Messages.getString("hub");
	/** 収集蓄積 転送設定*/
	public static final String HUB_TRANSFER = "HUB_TRF";
	/** 収集蓄積 転送設定*/
	public static final String STRING_HUB_TRANSFER = Messages.getString("hub.transfer");
	
	/** 環境構築 */
	public static final String INFRA = "INFRA";
	/** 環境構築 */
	public static final String STRING_INFRA = Messages.getString("infra");
	/** 環境構築設定 */
	public static final String INFRA_SETTING = "INFRA";
	/** 環境構築ファイル */
	public static final String INFRA_FILE = "INFRA_FILE";
	/** 環境構築設定 */
	public static final String STRING_INFRA_SETTING = Messages.getString("infra.setting");
	/** 環境構築ファイル */
	public static final String STRING_INFRA_FILE = Messages.getString("infra.file");

	/** エンタプライズ設定 */
	public static final String STRING_ENTERPRISE = Messages.getString("enterprise");

	/** レポーティング */
	public static final String STRING_REPORT = Messages.getString("report");
	/** レポーティング スケジュール */
	public static final String REPORT_SCHEDULE = "RPT_SCHEDULE";
	public static final String STRING_REPORT_SCHEDULE = Messages.getString("report.schedule");
	/** レポーティング テンプレート */
	public static final String REPORT_TEMPLATE = "RPT_TEMPLATE";
	public static final String STRING_REPORT_TEMPLATE = Messages.getString("report.template");

	/** ノードマップ */
	public static final String STRING_NODE_MAP = Messages.getString("nodemap");
	
	/** ノードマップ設定 */
	public static final String NODE_MAP_SETTING = "MAP_SETTING";
	public static final String NODE_MAP_IMAGE = "MAP_IMAGE";
	public static final String STRING_NODE_MAP_SETTING = Messages.getString("nodemap.setting");
	public static final String STRING_NODE_MAP_IMAGE = Messages.getString("nodemap.image");
	public static final String STRING_NODE_MAP_IMAGE_BG = Messages.getString("nodemap.image.bg");
	public static final String STRING_NODE_MAP_IMAGE_ICON = Messages.getString("nodemap.image.icon");
	
	/** ジョブマップ */
	public static final String STRING_JOB_MAP = Messages.getString("jobmap");
	/** ジョブマップ イメージ*/
	public static final String JOB_MAP_IMAGE = "JMP_IMAGE";
	public static final String STRING_JOB_MAP_IMAGE = Messages.getString("jobmap.icon");
	
	/** バックアップ */
	public static final String STRING_BACKUP_IMPORT = Messages.getString("backup.import");
	public static final String STRING_BACKUP_EXPORT = Messages.getString("backup.export");
	public static final String STRING_BACKUP_CLEAR = Messages.getString("backup.clear");
	
	/** 仮想化 */
	public static final String STRING_CLOUD = Messages.getString("cloud");
	/** 仮想化  ユーザ*/
	public static final String CLOUD_USER = "CLOUD_USER";
	public static final String STRING_CLOUD_USER = Messages.getString("cloud.user");

	/** 仮想化　サービス 監視*/
	public static final String CLOUD_MONITOR_SERVICE = "MON_CLOUD_SERVICE_CONDITION";
	public static final String STRING_CLOUD_MONITOR_SERVICE = Messages.getString("cloud.mon.service");
	/** 仮想化　課金 監視*/
	public static final String CLOUD_MONITOR_BILLING = "MON_CLOUD_SERVICE_BILLING";
	public static final String STRING_CLOUD_MONITOR_BILLING = Messages.getString("cloud.mon.billing");
	
	/** ログ件数監視*/
	public static final String STRING_MONITOR_LOGCOUNT = Messages.getString("logcount.monitor");
	/** バイナリファイル監視*/
	public static final String STRING_MONITOR_BINARYFILE = Messages.getString("binary.file.monitor");
	/** パケットキャプチャ監視*/
	public static final String STRING_MONITOR_PCAP = Messages.getString("packet.capture.monitor");
	/** 収集値統合監視*/
	public static final String STRING_MONITOR_INTEGRATION = Messages.getString("integration.monitor");
	/** 相関係数監視*/
	public static final String STRING_MONITOR_CORRELATION = Messages.getString("correlation.monitor");
	
	/** 構成情報取得*/
	public static final String PLATFORM_NODECONFIG = "PLT_NODE_CONFIG";
	public static final String STRING_PLATFORM_NODECONFIG = Messages.getString("node.config.setting");

	/** 同時実行制御キュー*/
	public static final String JOB_QUEUE  = "JOB_QUEUE";
	public static final String STRING_JOB_QUEUE = Messages.getString("jobqueue");
	

}
