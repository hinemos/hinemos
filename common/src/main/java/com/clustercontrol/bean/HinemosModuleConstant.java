/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hinemosの機能を定数として格納するクラス<BR>
 *
 * @version 6.1.0
 * @since 2.1.2
 */
public class HinemosModuleConstant {

	public enum ModuleType{
		// Hinemos Manager Monitor
		HINEMOS_MANAGER_MONITOR("MNG"), // HINEMOS_MANAGER_MONITOR = "MNG"

		// 共通プラットフォーム
		PLATFORM("PLT"), // PLATFORM  = "PLT"
		// 通知
		PLATFORM_NOTIFY("PLT_NTF"), // PLATFORM_NOTIFY = "PLT_NTF"
		// メールテンプレート
		PLATFORM_MAIL_TEMPLATE("PLT_MIL_TMP"), // PLATFORM_MAIL_TEMPLATE = "PLT_MIL_TMP"
		// カレンダ
		PLATFORM_CALENDAR("PLT_CAL"), // PLATFORM_CALENDAR = "PLT_CAL"
		// カレンダパターン
		PLATFORM_CALENDAR_PATTERN("PLT_CAL_PTN"), // PLATFORM_CALENDAR_PATTERN = "PLT_CAL_PTN"
		// アクセス
		PLATFORM_ACCESS("PLT_ACC"), // PLATFORM_ACCESS 	= "PLT_ACC"
		// 重要度判定
		PLATFORM_PRIORITY_JUDGMENT("PLT_PRI_JMT"), // PLATFORM_PRIORITY_JUDGMENT = "PLT_PRI_JMT"
		// リポジトリ
		PLATFORM_REPOSITORY("PLT_REP"), // PLATFORM_REPOSITORY = "PLT_REP"
		// リポジトリ
		PLATFORM_REPSITORY_NODE("PLT_REP_NOD"), // PLATFORM_REPSITORY_NODE = "PLT_REP_NOD"
		// リポジトリ
		PLATFORM_REPSITORY_SCOPE("PLT_REP_SCP"), // PLATFORM_REPSITORY_SCOPE = "PLT_REP_SCP"

		// 監視設定
		MONITOR("MON"), // MONITOR = "MON"
		// Hinemosエージェント監視
		MONITOR_AGENT("MON_AGT_B"), // MONITOR_AGENT = "MON_AGT_B"
		// カスタム監視 （数値）
		MONITOR_CUSTOM_N("MON_CUSTOM_N"), // MONITOR_CUSTOM_N = "MON_CUSTOM_N"
		// カスタム監視 （文字列）
		MONITOR_CUSTOM_S("MON_CUSTOM_S"), // MONITOR_CUSTOM_S = "MON_CUSTOM_S"	
		// HTTP監視（数値）
		MONITOR_HTTP_N("MON_HTP_N"), // MONITOR_HTTP_N = "MON_HTP_N"
		// HTTP監視（文字列）
		MONITOR_HTTP_S("MON_HTP_S"), // MONITOR_HTTP_S = "MON_HTP_S"
		// HTTP監視（シナリオ）
		MONITOR_HTTP_SCENARIO("MON_HTP_SCE"), // MONITOR_HTTP_SCENARIO = "MON_HTP_SCE"
		// リソース監視
		MONITOR_PERFORMANCE("MON_PRF_N"), // MONITOR_PERFORMANCE = "MON_PRF_N"
		// PING監視
		MONITOR_PING("MON_PNG_N"), // MONITOR_PING = "MON_PNG_N"
		// サービス・ポート監視
		MONITOR_PORT("MON_PRT_N"), // MONITOR_PORT = "MON_PRT_N"
		// プロセス監視
		MONITOR_PROCESS("MON_PRC_N"), // MONITOR_PROCESS = "MON_PRC_N"
		// SNMP監視（数値）
		MONITOR_SNMP_N("MON_SNMP_N"), // MONITOR_SNMP_N = "MON_SNMP_N"
		// SNMP監視（文字列）
		MONITOR_SNMP_S("MON_SNMP_S"), // MONITOR_SNMP_S = "MON_SNMP_S"
		// SNMPTRAP監視
		MONITOR_SNMPTRAP("MON_SNMP_TRP"), // MONITOR_SNMPTRAP = "MON_SNMP_TRP"
		// SQL監視（数値）
		MONITOR_SQL_N("MON_SQL_N"), // MONITOR_SQL_N = "MON_SQL_N"
		// SQL監視（文字列）
		MONITOR_SQL_S("MON_SQL_S"), // MONITOR_SQL_S = "MON_SQL_S"
		// システムログ監視（文字列）
		MONITOR_SYSTEMLOG("MON_SYSLOG_S"), // MONITOR_SYSTEMLOG = "MON_SYSLOG_S"
		// ログファイル監視
		MONITOR_LOGFILE("MON_LOGFILE_S"), // MONITOR_LOGFILE = "MON_LOGFILE_S"
		// ログ件数監視
		MONITOR_LOGCOUNT("MON_LOGCOUNT_N"), // MONITOR_LOGCOUNT = "MON_LOGCOUNT_N"
		// Windowsサービス監視
		MONITOR_WINSERVICE("MON_WINSERVICE_B"), // MONITOR_WINSERVICE = "MON_WINSERVICE_B"
		// Windowsイベント監視
		MONITOR_WINEVENT("MON_WINEVENT_S"), // MONITOR_WINEVENT = "MON_WINEVENT_S"
		// カスタムトラップ監視 （数値）
		MONITOR_CUSTOMTRAP_N("MON_CUSTOMTRAP_N"), // MONITOR_CUSTOMTRAP_N = "MON_CUSTOMTRAP_N"
		// カスタムトラップ監視 （文字列）
		MONITOR_CUSTOMTRAP_S("MON_CUSTOMTRAP_S"), // MONITOR_CUSTOMTRAP_S = "MON_CUSTOMTRAP_S"
		// 相関係数監視（数値）
		MONITOR_CORRELATION("MON_CORRELATION_N"), // MONITOR_CORRELATION = "MON_CORRELATION_N"
		// 収集値統合監視(真偽値)
		MONITOR_INTEGRATION("MON_COMPOUND_B"), // MONITOR_INTEGRATION = "MON_COMPOUND_B"
		// バイナリファイル監視 （バイナリ）
		MONITOR_BINARYFILE_BIN("MON_BINARYFILE_BIN"), // MONITOR_BINARYFILE_BIN = "MON_BINARYFILE_BIN"	
		// パケットキャプチャ （バイナリ）
		MONITOR_PCAP_BIN("MON_PCAP_BIN"), // MONITOR_PCAP_BIN = "MON_PCAP_BIN"	
		// スコープ
		MONITOR_SCOPE("MON_SCP"), // MONITOR_SCOPE = "MON_SCP"
		// ステータス
		MONITOR_STATUS("MON_STA"), // MONITOR_STATUS = "MON_STA"
		// イベント
		MONITOR_EVENT("MON_EVT"), // MONITOR_EVENT = "MON_EVT"
		// JMX
		MONITOR_JMX("MON_JMX_N"), // MONITOR_JMX = "MON_JMX_N"
		
		// ログフォーマット
		HUB_LOGFORMAT("HUB_LF"), // HUB_LOGFORMAT = "HUB_LF"
		// 収集蓄積 転送
		HUB_TRANSFER("HUB_TRF"), // HUB_TRANSFER = "HUB_TRF"
		
		// 性能管理
		PERFORMANCE("PRF"), // PERFORMANCE = "PRF"
		// 性能管理
		PERFORMANCE_RECORD("PRF_REC"), // PERFORMANCE_RECORD = "PRF_REC"
		// 性能管理
		PERFORMANCE_REALTIME("PRT_RT"), // PERFORMANCE_REALTIME = "PRT_RT"

		// ジョブ管理
		JOB("JOB"), // JOB = "JOB"
		// ジョブ管理
		JOB_MST("JOB_MST"), // JOB_MST  = "JOB_MST"
		// ジョブ管理
		JOB_SESSION("JOB_SES"), // JOB_SESSION = "JOB_SES"
		// ジョブ管理
		JOB_SESSION_DETAIL("JOB_SES_DTL"), // JOB_SESSION_DETAIL = "JOB_SES_DTL"
		// ジョブ管理
		JOB_SESSION_NODE("JOB_SES_NOD"), // JOB_SESSION_NODE = "JOB_SES_NOD"
		// ジョブ管理
		JOB_SCHEDULE_RUN("JOB_SCH_RUN"), // JOB_SCHEDULE_RUN  = "JOB_SCH_RUN"
		// ジョブ管理
		JOB_SCHEDULE_RUN_DETAIL("JOB_SCH_RUN_DTL"), // JOB_SCHEDULE_RUN_DETAIL  = "JOB_SCH_RUN_DTL"
		// ジョブ管理
		JOB_SESSION_FILE("JOB_SES_FIL"), // JOB_SESSION_FILE = "JOB_SES_FIL"
		// ジョブ管理
		JOB_KICK("JOB_KICK"), // JOB_KICK  = "JOB_KICK"
		// ジョブ管理
		JOBMAP_IMAGE_FILE("JOBMAP_IMAGE_FILE"), // JOBMAP_IMAGE_FILE  = "JOBMAP_IMAGE_FILE"

		// 自動デバイスサーチ
		REPOSITORY_DEVICE_SEARCH("REP_DS"), // REPOSITORY_DEVICE_SEARCH = "REP_DS"

		// Hinemos自身の処理
		SYSYTEM("SYS"), // SYSYTEM = "SYS"
		// メンテナンス
		SYSYTEM_MAINTENANCE("MAINTENANCE"), // SYSYTEM_MAINTENANCE = "MAINTENANCE"
		// セルフチェック
		SYSYTEM_SELFCHECK("SYS_SFC"), // SYSYTEM_SELFCHECK = "SYS_SFC"
		// セルフチェックのデフォルトID
		SYSYTEM_SELFCHECK_ID("DEFAULT"), // SYSYTEM_SELFCHECK_ID = "DEFAULT"

		// 環境構築
		INFRA("INFRA"), // INFRA = "INFRA"
		INFRA_FILE("INFRA_FILE"), // INFRA_FILE = "INFRA_FILE"

		// レポーティング
		REPORTING("REPORTING"), // REPORTING = "REPORTING"

		// 遠隔管理
		INQUIRY("INQUIRY"); // INQUIRY = "INQUIRY"

		private String type;

		ModuleType(String type) {
			this.type = type;
		}

		String getType() {
			return this.type;
		}

		public static ModuleType fromString(String type) {
			for (ModuleType t: ModuleType.values()) {
				if (t.type.equalsIgnoreCase(type)) {
					return t;
				}
			}
			return null;
			// OR throw new IllegalArgumentException("undefined " + type);
		}
	}

	// @deprecated (Should use enum directly)
	@Deprecated public static final String HINEMOS_MANAGER_MONITOR = ModuleType.HINEMOS_MANAGER_MONITOR.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PLATFORM = ModuleType.PLATFORM.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PLATFORM_NOTIFY = ModuleType.PLATFORM_NOTIFY.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PLATFORM_MAIL_TEMPLATE = ModuleType.PLATFORM_MAIL_TEMPLATE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PLATFORM_CALENDAR = ModuleType.PLATFORM_CALENDAR.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PLATFORM_CALENDAR_PATTERN = ModuleType.PLATFORM_CALENDAR_PATTERN.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PLATFORM_ACCESS = ModuleType.PLATFORM_ACCESS.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PLATFORM_PRIORITY_JUDGMENT = ModuleType.PLATFORM_PRIORITY_JUDGMENT.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PLATFORM_REPOSITORY = ModuleType.PLATFORM_REPOSITORY.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PLATFORM_REPSITORY_NODE = ModuleType.PLATFORM_REPSITORY_NODE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PLATFORM_REPSITORY_SCOPE = ModuleType.PLATFORM_REPSITORY_SCOPE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR = ModuleType.MONITOR.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_AGENT = ModuleType.MONITOR_AGENT.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_CUSTOM_N = ModuleType.MONITOR_CUSTOM_N.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_CUSTOM_S = ModuleType.MONITOR_CUSTOM_S.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_HTTP_N = ModuleType.MONITOR_HTTP_N.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_HTTP_S = ModuleType.MONITOR_HTTP_S.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_HTTP_SCENARIO = ModuleType.MONITOR_HTTP_SCENARIO.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_PERFORMANCE = ModuleType.MONITOR_PERFORMANCE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_PING = ModuleType.MONITOR_PING.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_PORT = ModuleType.MONITOR_PORT.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_PROCESS = ModuleType.MONITOR_PROCESS.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_SNMP_N = ModuleType.MONITOR_SNMP_N.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_SNMP_S = ModuleType.MONITOR_SNMP_S.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_SNMPTRAP = ModuleType.MONITOR_SNMPTRAP.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_SQL_N = ModuleType.MONITOR_SQL_N.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_SQL_S = ModuleType.MONITOR_SQL_S.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_SYSTEMLOG = ModuleType.MONITOR_SYSTEMLOG.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_LOGFILE = ModuleType.MONITOR_LOGFILE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_LOGCOUNT = ModuleType.MONITOR_LOGCOUNT.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_WINSERVICE = ModuleType.MONITOR_WINSERVICE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_WINEVENT = ModuleType.MONITOR_WINEVENT.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_CUSTOMTRAP_N = ModuleType.MONITOR_CUSTOMTRAP_N.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_CUSTOMTRAP_S = ModuleType.MONITOR_CUSTOMTRAP_S.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_CORRELATION = ModuleType.MONITOR_CORRELATION.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_INTEGRATION = ModuleType.MONITOR_INTEGRATION.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_BINARYFILE_BIN = ModuleType.MONITOR_BINARYFILE_BIN.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_PCAP_BIN = ModuleType.MONITOR_PCAP_BIN.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_SCOPE = ModuleType.MONITOR_SCOPE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_STATUS = ModuleType.MONITOR_STATUS.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_EVENT = ModuleType.MONITOR_EVENT.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String MONITOR_JMX = ModuleType.MONITOR_JMX.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String HUB_LOGFORMAT = ModuleType.HUB_LOGFORMAT.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String HUB_TRANSFER = ModuleType.HUB_TRANSFER.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PERFORMANCE = ModuleType.PERFORMANCE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PERFORMANCE_RECORD = ModuleType.PERFORMANCE_RECORD.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String PERFORMANCE_REALTIME = ModuleType.PERFORMANCE_REALTIME.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String JOB = ModuleType.JOB.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String JOB_MST = ModuleType.JOB_MST.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String JOB_SESSION = ModuleType.JOB_SESSION.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String JOB_SESSION_DETAIL = ModuleType.JOB_SESSION_DETAIL.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String JOB_SESSION_NODE = ModuleType.JOB_SESSION_NODE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String JOB_SCHEDULE_RUN = ModuleType.JOB_SCHEDULE_RUN.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String JOB_SCHEDULE_RUN_DETAIL = ModuleType.JOB_SCHEDULE_RUN_DETAIL.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String JOB_SESSION_FILE = ModuleType.JOB_SESSION_FILE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String JOB_KICK = ModuleType.JOB_KICK.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String JOBMAP_IMAGE_FILE = ModuleType.JOBMAP_IMAGE_FILE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String REPOSITORY_DEVICE_SEARCH = ModuleType.REPOSITORY_DEVICE_SEARCH.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String SYSYTEM = ModuleType.SYSYTEM.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String SYSYTEM_MAINTENANCE = ModuleType.SYSYTEM_MAINTENANCE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String SYSYTEM_SELFCHECK = ModuleType.SYSYTEM_SELFCHECK.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String SYSYTEM_SELFCHECK_ID = ModuleType.SYSYTEM_SELFCHECK_ID.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String INFRA = ModuleType.INFRA.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String INFRA_FILE = ModuleType.INFRA_FILE.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String REPORTING = ModuleType.REPORTING.getType();
	// @deprecated (Should use enum directly)
	@Deprecated public static final String INQUIRY = ModuleType.INQUIRY.getType();

	/** マネージャ操作ログタイトル */
	/** リポジトリ */
	public static final String LOG_PREFIX_REPOSITORY = "[Repository]";
	/** アクセス */
	public static final String LOG_PREFIX_ACCESS = "[Access]";
	/** カレンダ */
	public static final String LOG_PREFIX_CALENDAR = "[Calendar]";
	/** 通知 */
	public static final String LOG_PREFIX_NOTIFY = "[Notify]";
	/** 環境構築 */
	public static final String LOG_PREFIX_INFRA = "[Infra]";
	/** 監視設定 */
	public static final String LOG_PREFIX_MONITOR = "[Monitor]";
	/** システムログ監視 */
	public static final String LOG_PREFIX_MONITOR_SYSTEMLOG = "[Systemlog]";
	/** ログファイル監視 */
	public static final String LOG_PREFIX_MONITOR_LOGFILE = "[Logfile]";
	/** ログ件数監視 */
	public static final String LOG_PREFIX_MONITOR_LOGCOUNT = "[Logcount]";
	/** Hinemosエージェント監視 */
	public static final String LOG_PREFIX_MONITOR_AGENT = "[Agent]";
	/** カスタム監視 */
	public static final String LOG_PREFIX_MONITOR_CUSTOM = "[Custom]";
	/** HTTP監視 */
	public static final String LOG_PREFIX_MONITOR_HTTP = "[Http]";
	/** HTTP監視(Scenario) */
	public static final String LOG_PREFIX_MONITOR_HTTP_SCENARIO = "[HttpScenario]";
	/** プロセス監視 */
	public static final String LOG_PREFIX_MONITOR_PROCESS = "[Process]";
	/** SQL監視 */
	public static final String LOG_PREFIX_MONITOR_SQL = "[Sql]";
	/** SNMP監視 */
	public static final String LOG_PREFIX_MONITOR_SNMP = "[Snmp]";
	/** PING監視 */
	public static final String LOG_PREFIX_MONITOR_PING = "[Ping]";
	/** SNMPTRAP監視 */
	public static final String LOG_PREFIX_MONITOR_SNMPTRAP = "[Snmptrap]";
	/** リソース監視 */
	public static final String LOG_PREFIX_MONITOR_PERFORMANCE = "[Resource]";
	/** サービス・ポート監視 */
	public static final String LOG_PREFIX_MONITOR_PORT = "[Port]";
	/** Windowsサービス監視 */
	public static final String LOG_PREFIX_MONITOR_WINSERVICE = "[WinService]";
	/** Windowsイベント監視 */
	public static final String LOG_PREFIX_MONITOR_WINEVENT = "[WinEvent]";
	/** カスタム監視 */
	public static final String LOG_PREFIX_MONITOR_CUSTOMTRAP = "[Customtrap]";
	/** 相関係数監視 */
	public static final String LOG_PREFIX_MONITOR_CORRELATION = "[Correlation]";
	/** 収集値統合監視 */
	public static final String LOG_PREFIX_MONITOR_INTEGRATION = "[Compound]";
	/** バイナリファイル監視 */
	public static final String LOG_PREFIX_MONITOR_BINARY_FILE = "[BinaryFile]";
	/** パケットキャプチャ監視 */
	public static final String LOG_PREFIX_MONITOR_PCAP = "[PcketCapture]";
	/** JMX */	
	public static final String LOG_PREFIX_MONITOR_JMX = "[Jmx]";
	/** 性能管理 */
	public static final String LOG_PREFIX_PERFORMANCE = "[Collector]";
	/** ジョブ管理 */
	public static final String LOG_PREFIX_JOB = "[Job]";
	/** メンテナンス */
	public static final String LOG_PREFIX_MAINTENANCE = "[Maintenance]";
	/** メールテンプレート */
	public static final String LOG_PREFIX_MAIL_TEMPLATE = "[MailTemplate]";
	/** ノードマップ */
	public static final String LOG_PREFIX_NODEMAP = "[NodeMap]";
	/** 仮想化管理 */
	public static final String LOG_PREFIX_VM = "[VM]";
	/** レポーティング */
	public static final String LOG_PREFIX_REPORTING = "[Reporting]"; 
	/** 収集蓄積 */
	public static final String LOG_PREFIX_HUB = "[Hub]";
	/** 遠隔管理 */
	public static final String LOG_PREFIX_INQUIRY = "[Inquiry]";

	private static List<ExtensionType> extensionTypeList = new ArrayList<ExtensionType>();

	public static List<ExtensionType> getExtensionTypeList(){
		return extensionTypeList;
	}

	public static class ExtensionType{
		private String typeId;
		private int typeCode;
		private String stringType;

		public ExtensionType(String typeId, int typeCode, String stringType){
			this.typeId = typeId;
			this.typeCode = typeCode;
			this.stringType = stringType;
		}

		public String getTypeId() {
			return typeId;
		}

		public int getTypeCode() {
			return typeCode;
		}

		public String getStringType() {
			return stringType;
		}
	}

	public static void addExtensionType(ExtensionType extensionType){
		extensionTypeList.add(extensionType);
	}


	/**
	 * 機能略記が存在するかチェックします。
	 *
	 *
	 */
	public static boolean isExist(String typeId){
		ModuleType type = ModuleType.fromString(typeId);

		if(null != type) {
			return true;
		}

		if(!extensionTypeList.isEmpty()){
			for(ExtensionType extensionType: extensionTypeList){
				if(typeId.equals(extensionType.getTypeId())){
					return true;
				}
			}
		}
		return false;
	}
	
	private static final Map<ModuleType, String> CODEMAP = new HashMap<ModuleType, String>();
	static {
		CODEMAP.put(ModuleType.PLATFORM_ACCESS, "ACCESSCONTROL");
		CODEMAP.put(ModuleType.PLATFORM_PRIORITY_JUDGMENT, "PRIORITY_JUDGMENT");
		CODEMAP.put(ModuleType.PLATFORM_CALENDAR, "CALENDAR");
		CODEMAP.put(ModuleType.PLATFORM_CALENDAR_PATTERN, "CALENDAR_PATTERN");
		CODEMAP.put(ModuleType.PLATFORM_REPOSITORY, "REPOSITORY");
		CODEMAP.put(ModuleType.PLATFORM_NOTIFY, "NOTIFY");
		CODEMAP.put(ModuleType.PLATFORM_MAIL_TEMPLATE, "MAIL_TEMPLATE");
		CODEMAP.put(ModuleType.MONITOR, "MONITOR_SETTING");
		CODEMAP.put(ModuleType.MONITOR_AGENT, "AGENT_MONITOR");
		CODEMAP.put(ModuleType.MONITOR_CUSTOM_N, "CUSTOM_MONITOR_N");
		CODEMAP.put(ModuleType.MONITOR_CUSTOM_S, "CUSTOM_MONITOR_S");
		CODEMAP.put(ModuleType.MONITOR_HTTP_N, "HTTP_MONITOR_N");
		CODEMAP.put(ModuleType.MONITOR_HTTP_S, "HTTP_MONITOR_S");
		CODEMAP.put(ModuleType.MONITOR_HTTP_SCENARIO, "HTTP_MONITOR_SCENARIO");
		CODEMAP.put(ModuleType.MONITOR_PING, "PING_MONITOR");
		CODEMAP.put(ModuleType.MONITOR_PROCESS, "PROCESS_MONITOR");
		CODEMAP.put(ModuleType.MONITOR_SNMP_N, "SNMP_MONITOR_N");
		CODEMAP.put(ModuleType.MONITOR_SNMP_S, "SNMP_MONITOR_S");
		CODEMAP.put(ModuleType.MONITOR_SNMPTRAP, "SNMPTRAP_MONITOR");
		CODEMAP.put(ModuleType.MONITOR_SQL_N, "SQL_MONITOR_N");
		CODEMAP.put(ModuleType.MONITOR_SQL_S, "SQL_MONITOR_S");
		CODEMAP.put(ModuleType.MONITOR_PERFORMANCE, "PERFORMANCE_MONITOR");
		CODEMAP.put(ModuleType.MONITOR_PORT, "PORT_MONITOR");
		CODEMAP.put(ModuleType.MONITOR_WINSERVICE, "WINSERVICE_MONITOR");
		CODEMAP.put(ModuleType.MONITOR_WINEVENT, "WINEVENT_MONITOR");
		CODEMAP.put(ModuleType.MONITOR_JMX, "JMX_MONITOR");
		CODEMAP.put(ModuleType.HUB_LOGFORMAT, "LOG_FORMAT");
		CODEMAP.put(ModuleType.HUB_TRANSFER, "LOG_TRANSFER");
		CODEMAP.put(ModuleType.MONITOR_SYSTEMLOG, "SYSTEMLOG_MONITOR");
		CODEMAP.put(ModuleType.MONITOR_LOGFILE, "LOGFILE_MONITOR");
		CODEMAP.put(ModuleType.MONITOR_LOGCOUNT, "LOGCOUNT_MONITOR");
		CODEMAP.put(ModuleType.MONITOR_CORRELATION, "COLLECT_MONITOR");
		CODEMAP.put(ModuleType.MONITOR_INTEGRATION, "INTEGRATION_MONITOR");
		CODEMAP.put(ModuleType.PERFORMANCE, "PERFORMANCE");
		CODEMAP.put(ModuleType.JOB, "JOB_MANAGEMENT");
		CODEMAP.put(ModuleType.JOB_MST, "JOB_MANAGEMENT");
		CODEMAP.put(ModuleType.JOB_KICK, "JOB_KICK");
		CODEMAP.put(ModuleType.SYSYTEM_MAINTENANCE, "MAINTENANCE_NAME");
		CODEMAP.put(ModuleType.SYSYTEM_SELFCHECK, "SELFCHECK_NAME");
		CODEMAP.put(ModuleType.INFRA, "INFRA_MANAGEMENT");
		CODEMAP.put(ModuleType.INFRA_FILE, "INFRA_FILE_MANAGER");
		CODEMAP.put(ModuleType.REPORTING, "REPORTING");
		CODEMAP.put(ModuleType.HINEMOS_MANAGER_MONITOR, "MNG");
		CODEMAP.put(ModuleType.MONITOR_CUSTOMTRAP_N, "CUSTOMTRAP_MONITOR_N");
		CODEMAP.put(ModuleType.MONITOR_CUSTOMTRAP_S, "CUSTOMTRAP_MONITOR_S");
		CODEMAP.put(ModuleType.MONITOR_BINARYFILE_BIN, "BINARYFILE_MONITOR_BIN");
		CODEMAP.put(ModuleType.MONITOR_PCAP_BIN, "PCAP_MONITOR_BIN");
	}

	public static String nameToMessageCode(String typeId) {
		ModuleType type = ModuleType.fromString(typeId);
		if(type != null && CODEMAP.containsKey(type)) {
			return CODEMAP.get(type);
		}
		if(!extensionTypeList.isEmpty()){
			for(ExtensionType extensionType: extensionTypeList){
				if(typeId.equals(extensionType.getTypeId())){
					return extensionType.getStringType();
				}
			}
		}
		return "";
	}

	private HinemosModuleConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
