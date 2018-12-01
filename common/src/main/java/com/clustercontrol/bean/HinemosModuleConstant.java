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
		HINEMOS_MANAGER_MONITOR("MNG"), //public static final String HINEMOS_MANAGER_MONITOR = "MNG";

		// 共通プラットフォーム
		PLATFORM("PLT"), //public static final String PLATFORM  = "PLT";
		// 通知
		PLATFORM_NOTIFY("PLT_NTF"), //public static final String PLATFORM_NOTIFY = "PLT_NTF";
		// メールテンプレート
		PLATFORM_MAIL_TEMPLATE("PLT_MIL_TMP"), //public static final String PLATFORM_MAIL_TEMPLATE = "PLT_MIL_TMP";
		// カレンダ
		PLATFORM_CALENDAR("PLT_CAL"), //public static final String PLATFORM_CALENDAR = "PLT_CAL";
		// カレンダパターン
		PLATFORM_CALENDAR_PATTERN("PLT_CAL_PTN"), //public static final String PLATFORM_CALENDAR_PATTERN = "PLT_CAL_PTN";
		// アクセス
		PLATFORM_ACCESS("PLT_ACC"), //public static final String PLATFORM_ACCESS 	= "PLT_ACC";
		// 重要度判定
		PLATFORM_PRIORITY_JUDGMENT("PLT_PRI_JMT"), //public static final String PLATFORM_PRIORITY_JUDGMENT = "PLT_PRI_JMT";
		// リポジトリ
		PLATFORM_REPOSITORY("PLT_REP"), //public static final String PLATFORM_REPOSITORY = "PLT_REP";
		// リポジトリ
		PLATFORM_REPSITORY_NODE("PLT_REP_NOD"), //public static final String PLATFORM_REPSITORY_NODE = "PLT_REP_NOD";
		// リポジトリ
		PLATFORM_REPSITORY_SCOPE("PLT_REP_SCP"), //public static final String PLATFORM_REPSITORY_SCOPE = "PLT_REP_SCP";

		// 監視設定
		MONITOR("MON"), //public static final String MONITOR = "MON";
		// Hinemosエージェント監視
		MONITOR_AGENT("MON_AGT_B"), //public static final String MONITOR_AGENT = "MON_AGT_B";
		// カスタム監視 （数値）
		MONITOR_CUSTOM_N("MON_CUSTOM_N"), //public static final String MONITOR_CUSTOM_N = "MON_CUSTOM_N";
		// カスタム監視 （文字列）
		MONITOR_CUSTOM_S("MON_CUSTOM_S"), //public static final String MONITOR_CUSTOM_S = "MON_CUSTOM_S";	
		// HTTP監視（数値）
		MONITOR_HTTP_N("MON_HTP_N"), //public static final String MONITOR_HTTP_N = "MON_HTP_N";
		// HTTP監視（文字列）
		MONITOR_HTTP_S("MON_HTP_S"), //public static final String MONITOR_HTTP_S = "MON_HTP_S";
		// HTTP監視（シナリオ）
		MONITOR_HTTP_SCENARIO("MON_HTP_SCE"), //public static final String MONITOR_HTTP_SCENARIO = "MON_HTP_SCE";
		// リソース監視
		MONITOR_PERFORMANCE("MON_PRF_N"), //public static final String MONITOR_PERFORMANCE = "MON_PRF_N";
		// PING監視
		MONITOR_PING("MON_PNG_N"), //public static final String MONITOR_PING = "MON_PNG_N";
		// サービス・ポート監視
		MONITOR_PORT("MON_PRT_N"), //public static final String MONITOR_PORT = "MON_PRT_N";
		// プロセス監視
		MONITOR_PROCESS("MON_PRC_N"), //public static final String MONITOR_PROCESS = "MON_PRC_N";
		// SNMP監視（数値）
		MONITOR_SNMP_N("MON_SNMP_N"), //public static final String MONITOR_SNMP_N = "MON_SNMP_N";
		// SNMP監視（文字列）
		MONITOR_SNMP_S("MON_SNMP_S"), //public static final String MONITOR_SNMP_S = "MON_SNMP_S";
		// SNMPTRAP監視
		MONITOR_SNMPTRAP("MON_SNMP_TRP"), //public static final String MONITOR_SNMPTRAP = "MON_SNMP_TRP";
		// SQL監視（数値）
		MONITOR_SQL_N("MON_SQL_N"), //public static final String MONITOR_SQL_N = "MON_SQL_N";
		// SQL監視（文字列）
		MONITOR_SQL_S("MON_SQL_S"), //public static final String MONITOR_SQL_S = "MON_SQL_S";
		// システムログ監視（文字列）
		MONITOR_SYSTEMLOG("MON_SYSLOG_S"), //public static final String MONITOR_SYSTEMLOG = "MON_SYSLOG_S";
		// ログファイル監視
		MONITOR_LOGFILE("MON_LOGFILE_S"), //public static final String MONITOR_LOGFILE = "MON_LOGFILE_S";
		// ログ件数監視
		MONITOR_LOGCOUNT("MON_LOGCOUNT_N"), //public static final String MONITOR_LOGCOUNT = "MON_LOGCOUNT_N";
		// Windowsサービス監視
		MONITOR_WINSERVICE("MON_WINSERVICE_B"), //public static final String MONITOR_WINSERVICE = "MON_WINSERVICE_B";
		// Windowsイベント監視
		MONITOR_WINEVENT("MON_WINEVENT_S"), //public static final String MONITOR_WINEVENT = "MON_WINEVENT_S";
		// カスタムトラップ監視 （数値）
		MONITOR_CUSTOMTRAP_N("MON_CUSTOMTRAP_N"), //public static final String MONITOR_CUSTOMTRAP_N = "MON_CUSTOMTRAP_N";
		// カスタムトラップ監視 （文字列）
		MONITOR_CUSTOMTRAP_S("MON_CUSTOMTRAP_S"), //public static final String MONITOR_CUSTOMTRAP_S = "MON_CUSTOMTRAP_S";
		// 相関係数監視（数値）
		MONITOR_CORRELATION("MON_CORRELATION_N"), //public static final String MONITOR_CORRELATION = "MON_CORRELATION_N";
		// 収集値統合監視(真偽値)
		MONITOR_INTEGRATION("MON_COMPOUND_B"), //public static final String MONITOR_INTEGRATION = "MON_COMPOUND_B";
		// バイナリファイル監視 （バイナリ）
		MONITOR_BINARYFILE_BIN("MON_BINARYFILE_BIN"), //public static final String MONITOR_BINARYFILE_BIN = "MON_BINARYFILE_BIN";	
		// パケットキャプチャ （バイナリ）
		MONITOR_PCAP_BIN("MON_PCAP_BIN"), //public static final String MONITOR_PCAP_BIN = "MON_PCAP_BIN";	
		// スコープ
		MONITOR_SCOPE("MON_SCP"), //public static final String MONITOR_SCOPE = "MON_SCP";
		// ステータス
		MONITOR_STATUS("MON_STA"), //public static final String MONITOR_STATUS = "MON_STA";
		// イベント
		MONITOR_EVENT("MON_EVT"), //public static final String MONITOR_EVENT = "MON_EVT";
		// JMX
		MONITOR_JMX("MON_JMX_N"), //public static final String MONITOR_JMX = "MON_JMX_N";
		
		// ログフォーマット
		HUB_LOGFORMAT("HUB_LF"), //public static final String HUB_LOGFORMAT = "HUB_LF";
		// 収集蓄積 転送
		HUB_TRANSFER("HUB_TRF"), //public static final String HUB_TRANSFER = "HUB_TRF";
		
		// 性能管理
		PERFORMANCE("PRF"), //public static final String PERFORMANCE = "PRF";
		// 性能管理
		PERFORMANCE_RECORD("PRF_REC"), //public static final String PERFORMANCE_RECORD = "PRF_REC";
		// 性能管理
		PERFORMANCE_REALTIME("PRT_RT"), //public static final String PERFORMANCE_REALTIME = "PRT_RT";

		// ジョブ管理
		JOB("JOB"), //public static final String JOB = "JOB";
		// ジョブ管理
		JOB_MST("JOB_MST"), //public static final String JOB_MST  = "JOB_MST";
		// ジョブ管理
		JOB_SESSION("JOB_SES"), //public static final String JOB_SESSION = "JOB_SES";
		// ジョブ管理
		JOB_SESSION_DETAIL("JOB_SES_DTL"), //public static final String JOB_SESSION_DETAIL = "JOB_SES_DTL";
		// ジョブ管理
		JOB_SESSION_NODE("JOB_SES_NOD"), //public static final String JOB_SESSION_NODE = "JOB_SES_NOD";
		// ジョブ管理
		JOB_SCHEDULE_RUN("JOB_SCH_RUN"), //public static final String JOB_SCHEDULE_RUN  = "JOB_SCH_RUN";
		// ジョブ管理
		JOB_SCHEDULE_RUN_DETAIL("JOB_SCH_RUN_DTL"), //public static final String JOB_SCHEDULE_RUN_DETAIL  = "JOB_SCH_RUN_DTL";
		// ジョブ管理
		JOB_SESSION_FILE("JOB_SES_FIL"), //public static final String JOB_SESSION_FILE = "JOB_SES_FIL";
		// ジョブ管理
		JOB_KICK("JOB_KICK"), //public static final String JOB_KICK  = "JOB_KICK";
		// ジョブ管理
		JOBMAP_IMAGE_FILE("JOBMAP_IMAGE_FILE"), //public static final String JOBMAP_IMAGE_FILE  = "JOBMAP_IMAGE_FILE";

		// 自動デバイスサーチ
		REPOSITORY_DEVICE_SEARCH("REP_DS"), //public static final String REPOSITORY_DEVICE_SEARCH = "REP_DS";

		// Hinemos自身の処理
		SYSYTEM("SYS"), //public static final String SYSYTEM = "SYS";
		// メンテナンス
		SYSYTEM_MAINTENANCE("MAINTENANCE"), //public static final String SYSYTEM_MAINTENANCE = "MAINTENANCE";
		// セルフチェック
		SYSYTEM_SELFCHECK("SYS_SFC"), //public static final String SYSYTEM_SELFCHECK = "SYS_SFC";
		// セルフチェックのデフォルトID
		SYSYTEM_SELFCHECK_ID("DEFAULT"), //public static final String SYSYTEM_SELFCHECK_ID = "DEFAULT";

		// 環境構築
		INFRA("INFRA"), //public static final String INFRA = "INFRA";
		INFRA_FILE("INFRA_FILE"), //public static final String INFRA_FILE = "INFRA_FILE";

		// レポーティング
		REPORTING("REPORTING"), //public static final String REPORTING = "REPORTING"; 

		// 遠隔管理
		INQUIRY("INQUIRY"); //public static final String INQUIRY = "INQUIRY"; 

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

	@Deprecated public static final String HINEMOS_MANAGER_MONITOR = ModuleType.HINEMOS_MANAGER_MONITOR.getType();
	@Deprecated public static final String PLATFORM = ModuleType.PLATFORM.getType();
	@Deprecated public static final String PLATFORM_NOTIFY = ModuleType.PLATFORM_NOTIFY.getType();
	@Deprecated public static final String PLATFORM_MAIL_TEMPLATE = ModuleType.PLATFORM_MAIL_TEMPLATE.getType();
	@Deprecated public static final String PLATFORM_CALENDAR = ModuleType.PLATFORM_CALENDAR.getType();
	@Deprecated public static final String PLATFORM_CALENDAR_PATTERN = ModuleType.PLATFORM_CALENDAR_PATTERN.getType();
	@Deprecated public static final String PLATFORM_ACCESS = ModuleType.PLATFORM_ACCESS.getType();
	@Deprecated public static final String PLATFORM_PRIORITY_JUDGMENT = ModuleType.PLATFORM_PRIORITY_JUDGMENT.getType();
	@Deprecated public static final String PLATFORM_REPOSITORY = ModuleType.PLATFORM_REPOSITORY.getType();
	@Deprecated public static final String PLATFORM_REPSITORY_NODE = ModuleType.PLATFORM_REPSITORY_NODE.getType();
	@Deprecated public static final String PLATFORM_REPSITORY_SCOPE = ModuleType.PLATFORM_REPSITORY_SCOPE.getType();
	@Deprecated public static final String MONITOR = ModuleType.MONITOR.getType();
	@Deprecated public static final String MONITOR_AGENT = ModuleType.MONITOR_AGENT.getType();
	@Deprecated public static final String MONITOR_CUSTOM_N = ModuleType.MONITOR_CUSTOM_N.getType();
	@Deprecated public static final String MONITOR_CUSTOM_S = ModuleType.MONITOR_CUSTOM_S.getType();
	@Deprecated public static final String MONITOR_HTTP_N = ModuleType.MONITOR_HTTP_N.getType();
	@Deprecated public static final String MONITOR_HTTP_S = ModuleType.MONITOR_HTTP_S.getType();
	@Deprecated public static final String MONITOR_HTTP_SCENARIO = ModuleType.MONITOR_HTTP_SCENARIO.getType();
	@Deprecated public static final String MONITOR_PERFORMANCE = ModuleType.MONITOR_PERFORMANCE.getType();
	@Deprecated public static final String MONITOR_PING = ModuleType.MONITOR_PING.getType();
	@Deprecated public static final String MONITOR_PORT = ModuleType.MONITOR_PORT.getType();
	@Deprecated public static final String MONITOR_PROCESS = ModuleType.MONITOR_PROCESS.getType();
	@Deprecated public static final String MONITOR_SNMP_N = ModuleType.MONITOR_SNMP_N.getType();
	@Deprecated public static final String MONITOR_SNMP_S = ModuleType.MONITOR_SNMP_S.getType();
	@Deprecated public static final String MONITOR_SNMPTRAP = ModuleType.MONITOR_SNMPTRAP.getType();
	@Deprecated public static final String MONITOR_SQL_N = ModuleType.MONITOR_SQL_N.getType();
	@Deprecated public static final String MONITOR_SQL_S = ModuleType.MONITOR_SQL_S.getType();
	@Deprecated public static final String MONITOR_SYSTEMLOG = ModuleType.MONITOR_SYSTEMLOG.getType();
	@Deprecated public static final String MONITOR_LOGFILE = ModuleType.MONITOR_LOGFILE.getType();
	@Deprecated public static final String MONITOR_LOGCOUNT = ModuleType.MONITOR_LOGCOUNT.getType();
	@Deprecated public static final String MONITOR_WINSERVICE = ModuleType.MONITOR_WINSERVICE.getType();
	@Deprecated public static final String MONITOR_WINEVENT = ModuleType.MONITOR_WINEVENT.getType();
	@Deprecated public static final String MONITOR_CUSTOMTRAP_N = ModuleType.MONITOR_CUSTOMTRAP_N.getType();
	@Deprecated public static final String MONITOR_CUSTOMTRAP_S = ModuleType.MONITOR_CUSTOMTRAP_S.getType();
	@Deprecated public static final String MONITOR_CORRELATION = ModuleType.MONITOR_CORRELATION.getType();
	@Deprecated public static final String MONITOR_INTEGRATION = ModuleType.MONITOR_INTEGRATION.getType();
	@Deprecated public static final String MONITOR_BINARYFILE_BIN = ModuleType.MONITOR_BINARYFILE_BIN.getType();
	@Deprecated public static final String MONITOR_PCAP_BIN = ModuleType.MONITOR_PCAP_BIN.getType();
	@Deprecated public static final String MONITOR_SCOPE = ModuleType.MONITOR_SCOPE.getType();
	@Deprecated public static final String MONITOR_STATUS = ModuleType.MONITOR_STATUS.getType();
	@Deprecated public static final String MONITOR_EVENT = ModuleType.MONITOR_EVENT.getType();
	@Deprecated public static final String MONITOR_JMX = ModuleType.MONITOR_JMX.getType();
	@Deprecated public static final String HUB_LOGFORMAT = ModuleType.HUB_LOGFORMAT.getType();
	@Deprecated public static final String HUB_TRANSFER = ModuleType.HUB_TRANSFER.getType();
	@Deprecated public static final String PERFORMANCE = ModuleType.PERFORMANCE.getType();
	@Deprecated public static final String PERFORMANCE_RECORD = ModuleType.PERFORMANCE_RECORD.getType();
	@Deprecated public static final String PERFORMANCE_REALTIME = ModuleType.PERFORMANCE_REALTIME.getType();
	@Deprecated public static final String JOB = ModuleType.JOB.getType();
	@Deprecated public static final String JOB_MST = ModuleType.JOB_MST.getType();
	@Deprecated public static final String JOB_SESSION = ModuleType.JOB_SESSION.getType();
	@Deprecated public static final String JOB_SESSION_DETAIL = ModuleType.JOB_SESSION_DETAIL.getType();
	@Deprecated public static final String JOB_SESSION_NODE = ModuleType.JOB_SESSION_NODE.getType();
	@Deprecated public static final String JOB_SCHEDULE_RUN = ModuleType.JOB_SCHEDULE_RUN.getType();
	@Deprecated public static final String JOB_SCHEDULE_RUN_DETAIL = ModuleType.JOB_SCHEDULE_RUN_DETAIL.getType();
	@Deprecated public static final String JOB_SESSION_FILE = ModuleType.JOB_SESSION_FILE.getType();
	@Deprecated public static final String JOB_KICK = ModuleType.JOB_KICK.getType();
	@Deprecated public static final String JOBMAP_IMAGE_FILE = ModuleType.JOBMAP_IMAGE_FILE.getType();
	@Deprecated public static final String REPOSITORY_DEVICE_SEARCH = ModuleType.REPOSITORY_DEVICE_SEARCH.getType();
	@Deprecated public static final String SYSYTEM = ModuleType.SYSYTEM.getType();
	@Deprecated public static final String SYSYTEM_MAINTENANCE = ModuleType.SYSYTEM_MAINTENANCE.getType();
	@Deprecated public static final String SYSYTEM_SELFCHECK = ModuleType.SYSYTEM_SELFCHECK.getType();
	@Deprecated public static final String SYSYTEM_SELFCHECK_ID = ModuleType.SYSYTEM_SELFCHECK_ID.getType();
	@Deprecated public static final String INFRA = ModuleType.INFRA.getType();
	@Deprecated public static final String INFRA_FILE = ModuleType.INFRA_FILE.getType();
	@Deprecated public static final String REPORTING = ModuleType.REPORTING.getType();
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
}
