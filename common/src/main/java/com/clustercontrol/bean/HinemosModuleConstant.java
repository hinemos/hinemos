/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.util.ArrayList;
import java.util.Collections;
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

	/** Hinemos Manager Monitor */
	public static final String HINEMOS_MANAGER_MONITOR = "MNG";

	/** 共通プラットフォーム*/
	public static final String PLATFORM  = "PLT";
	/** 通知 */
	public static final String PLATFORM_NOTIFY = "PLT_NTF";
	/** メールテンプレート*/
	public static final String PLATFORM_MAIL_TEMPLATE = "PLT_MIL_TMP";
	/** カレンダ */
	public static final String PLATFORM_CALENDAR = "PLT_CAL";
	/** カレンダパターン */
	public static final String PLATFORM_CALENDAR_PATTERN = "PLT_CAL_PTN";
	/** アクセス */
	public static final String PLATFORM_ACCESS 	= "PLT_ACC";
	/** 重要度判定 */
	public static final String PLATFORM_PRIORITY_JUDGMENT = "PLT_PRI_JMT";
	/** リポジトリ */
	public static final String PLATFORM_REPOSITORY = "PLT_REP";
	/** リポジトリ */
	public static final String PLATFORM_REPSITORY_NODE = "PLT_REP_NOD";
	/** リポジトリ */
	public static final String PLATFORM_REPSITORY_SCOPE = "PLT_REP_SCP";

	/** 監視設定 */
	public static final String MONITOR = "MON";
	/** Hinemosエージェント監視 */
	public static final String MONITOR_AGENT = "MON_AGT_B";
	/** カスタム監視 （数値）*/
	public static final String MONITOR_CUSTOM_N = "MON_CUSTOM_N";
	/** カスタム監視 （文字列） */
	public static final String MONITOR_CUSTOM_S = "MON_CUSTOM_S";	
	/** HTTP監視（数値） */
	public static final String MONITOR_HTTP_N = "MON_HTP_N";
	/** HTTP監視（文字列） */
	public static final String MONITOR_HTTP_S = "MON_HTP_S";
	/** HTTP監視（シナリオ） */
	public static final String MONITOR_HTTP_SCENARIO = "MON_HTP_SCE";
	/** リソース監視 */
	public static final String MONITOR_PERFORMANCE = "MON_PRF_N";
	/** PING監視 */
	public static final String MONITOR_PING = "MON_PNG_N";
	/** サービス・ポート監視 */
	public static final String MONITOR_PORT = "MON_PRT_N";
	/** プロセス監視 */
	public static final String MONITOR_PROCESS = "MON_PRC_N";
	/** SNMP監視（数値） */
	public static final String MONITOR_SNMP_N = "MON_SNMP_N";
	/** SNMP監視（文字列） */
	public static final String MONITOR_SNMP_S = "MON_SNMP_S";
	/** SNMPTRAP監視 */
	public static final String MONITOR_SNMPTRAP = "MON_SNMP_TRP";
	/** SQL監視（数値） */
	public static final String MONITOR_SQL_N = "MON_SQL_N";
	/** SQL監視（文字列） */
	public static final String MONITOR_SQL_S = "MON_SQL_S";
	/** システムログ監視（文字列） */
	public static final String MONITOR_SYSTEMLOG = "MON_SYSLOG_S";
	/** ログファイル監視 */
	public static final String MONITOR_LOGFILE = "MON_LOGFILE_S";
	/** ログ件数監視 */
	public static final String MONITOR_LOGCOUNT = "MON_LOGCOUNT_N";
	/** Windowsサービス監視 */
	public static final String MONITOR_WINSERVICE = "MON_WINSERVICE_B";
	/** Windowsイベント監視 */
	public static final String MONITOR_WINEVENT = "MON_WINEVENT_S";
	/** カスタムトラップ監視 （数値）*/
	public static final String MONITOR_CUSTOMTRAP_N = "MON_CUSTOMTRAP_N";
	/** カスタムトラップ監視 （文字列） */
	public static final String MONITOR_CUSTOMTRAP_S = "MON_CUSTOMTRAP_S";
	/** 相関係数監視（数値） */
	public static final String MONITOR_CORRELATION = "MON_CORRELATION_N";
	/** 収集値統合監視(真偽値) */
	public static final String MONITOR_INTEGRATION = "MON_COMPOUND_B";
	/** バイナリファイル監視 （バイナリ） */
	public static final String MONITOR_BINARYFILE_BIN = "MON_BINARYFILE_BIN";	
	/** パケットキャプチャ （バイナリ） */
	public static final String MONITOR_PCAP_BIN = "MON_PCAP_BIN";	
	/** スコープ */
	public static final String MONITOR_SCOPE = "MON_SCP";
	/** ステータス */
	public static final String MONITOR_STATUS = "MON_STA";
	/** イベント */
	public static final String MONITOR_EVENT = "MON_EVT";
	/** JMX */
	public static final String MONITOR_JMX = "MON_JMX_N";
	
	/** ログフォーマット */
	public static final String HUB_LOGFORMAT = "HUB_LF";
	/** 収集蓄積 転送 */
	public static final String HUB_TRANSFER = "HUB_TRF";
	
	/** 性能管理 */
	public static final String PERFORMANCE = "PRF";
	/** 性能管理 */
	public static final String PERFORMANCE_RECORD = "PRF_REC";
	/** 性能管理 */
	public static final String PERFORMANCE_REALTIME = "PRT_RT";

	/** ジョブ管理 */
	public static final String JOB = "JOB";
	/** ジョブ管理 */
	public static final String JOB_MST  = "JOB_MST";
	/** ジョブ管理 */
	public static final String JOB_SESSION = "JOB_SES";
	/** ジョブ管理 */
	public static final String JOB_SESSION_DETAIL = "JOB_SES_DTL";
	/** ジョブ管理 */
	public static final String JOB_SESSION_NODE = "JOB_SES_NOD";
	/** ジョブ管理 */
	public static final String JOB_SCHEDULE_RUN  = "JOB_SCH_RUN";
	/** ジョブ管理 */
	public static final String JOB_SCHEDULE_RUN_DETAIL  = "JOB_SCH_RUN_DTL";
	/** ジョブ管理 */
	public static final String JOB_SESSION_FILE = "JOB_SES_FIL";
	/** ジョブ管理 */
	public static final String JOB_KICK  = "JOB_KICK";
	/** ジョブ管理 */
	public static final String JOBMAP_IMAGE_FILE  = "JOBMAP_IMAGE_FILE";

	/** 自動デバイスサーチ */
	public static final String REPOSITORY_DEVICE_SEARCH = "REP_DS";

	/**Hinemos自身の処理*/
	public static final String SYSYTEM = "SYS";
	/** メンテナンス */
	public static final String SYSYTEM_MAINTENANCE = "MAINTENANCE";
	/** セルフチェック */
	public static final String SYSYTEM_SELFCHECK = "SYS_SFC";
	/** セルフチェックのデフォルトID */
	public static final String SYSYTEM_SELFCHECK_ID = "DEFAULT";

	/** 環境構築 */
	public static final String INFRA = "INFRA";
	public static final String INFRA_FILE = "INFRA_FILE";

	/** レポーティング */
	public static final String REPORTING = "REPORTING"; 

	/** 遠隔管理 */
	public static final String INQUIRY = "INQUIRY"; 
	
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
		switch(typeId){
		case PLATFORM:
		case PLATFORM_NOTIFY:
		case PLATFORM_MAIL_TEMPLATE:
		case PLATFORM_CALENDAR:
		case PLATFORM_CALENDAR_PATTERN:
		case PLATFORM_ACCESS:
		case PLATFORM_PRIORITY_JUDGMENT:
		case PLATFORM_REPOSITORY:
		case PLATFORM_REPSITORY_NODE:
		case PLATFORM_REPSITORY_SCOPE:
		case MONITOR:
		case MONITOR_AGENT:
		case MONITOR_CUSTOM_N:
		case MONITOR_CUSTOM_S:
		case MONITOR_HTTP_N:
		case MONITOR_HTTP_S:
		case MONITOR_HTTP_SCENARIO:
		case MONITOR_LOGFILE:
		case MONITOR_LOGCOUNT:
		case MONITOR_PERFORMANCE:
		case MONITOR_PING:
		case MONITOR_PORT:
		case MONITOR_PROCESS:
		case MONITOR_SNMP_N:
		case MONITOR_SNMP_S:
		case MONITOR_SNMPTRAP:
		case MONITOR_SQL_N:
		case MONITOR_SQL_S:
		case MONITOR_SYSTEMLOG:
		case MONITOR_SCOPE:
		case MONITOR_STATUS:
		case MONITOR_EVENT:
		case MONITOR_WINSERVICE:
		case MONITOR_WINEVENT:
		case MONITOR_JMX:
		case MONITOR_CUSTOMTRAP_N:
		case MONITOR_CUSTOMTRAP_S:
		case MONITOR_CORRELATION:
		case MONITOR_INTEGRATION:
		case MONITOR_BINARYFILE_BIN:
		case MONITOR_PCAP_BIN:
		case HUB_LOGFORMAT:
		case HUB_TRANSFER:
		case PERFORMANCE:
		case PERFORMANCE_RECORD:
		case PERFORMANCE_REALTIME:
		case JOB:
		case JOB_MST:
		case JOB_SESSION:
		case JOB_SESSION_DETAIL:
		case JOB_SESSION_NODE:
		case JOB_KICK:
		case JOB_SCHEDULE_RUN_DETAIL:
		case JOB_SESSION_FILE:
		case SYSYTEM:
		case SYSYTEM_MAINTENANCE:
		case SYSYTEM_SELFCHECK:
		case INFRA:
		case INFRA_FILE:
		case REPORTING:
			return true;
		default:
			if(!extensionTypeList.isEmpty()){
				for(ExtensionType extensionType: extensionTypeList){
					if(typeId.equals(extensionType.getTypeId())){
						return true;
					}
				}
			}
			return false;
		}
	}
	
	public static String nameToMessageCode(String str) {
		final Map<String, String> codeMap = Collections.unmodifiableMap(new HashMap<String, String>() {{
			put(PLATFORM_ACCESS, "ACCESSCONTROL");
			put(PLATFORM_PRIORITY_JUDGMENT, "PRIORITY_JUDGMENT");
			put(PLATFORM_CALENDAR, "CALENDAR");
			put(PLATFORM_CALENDAR_PATTERN, "CALENDAR_PATTERN");
			put(PLATFORM_REPOSITORY, "REPOSITORY");
			put(PLATFORM_NOTIFY, "NOTIFY");
			put(PLATFORM_MAIL_TEMPLATE, "MAIL_TEMPLATE");
			put(MONITOR, "MONITOR_SETTING");
			put(MONITOR_AGENT, "AGENT_MONITOR");
			put(MONITOR_CUSTOM_N, "CUSTOM_MONITOR_N");
			put(MONITOR_CUSTOM_S, "CUSTOM_MONITOR_S");
			put(MONITOR_HTTP_N, "HTTP_MONITOR_N");
			put(MONITOR_HTTP_S, "HTTP_MONITOR_S");
			put(MONITOR_HTTP_SCENARIO, "HTTP_MONITOR_SCENARIO");
			put(MONITOR_PING, "PING_MONITOR");
			put(MONITOR_PROCESS, "PROCESS_MONITOR");
			put(MONITOR_SNMP_N, "SNMP_MONITOR_N");
			put(MONITOR_SNMP_S, "SNMP_MONITOR_S");
			put(MONITOR_SNMPTRAP, "SNMPTRAP_MONITOR");
			put(MONITOR_SQL_N, "SQL_MONITOR_N");
			put(MONITOR_SQL_S, "SQL_MONITOR_S");
			put(MONITOR_PERFORMANCE, "PERFORMANCE_MONITOR");
			put(MONITOR_PORT, "PORT_MONITOR");
			put(MONITOR_WINSERVICE, "WINSERVICE_MONITOR");
			put(MONITOR_WINEVENT, "WINEVENT_MONITOR");
			put(MONITOR_JMX, "JMX_MONITOR");
			put(HUB_LOGFORMAT, "LOG_FORMAT");
			put(HUB_TRANSFER, "LOG_TRANSFER");
			put(MONITOR_SYSTEMLOG, "SYSTEMLOG_MONITOR");
			put(MONITOR_LOGFILE, "LOGFILE_MONITOR");
			put(MONITOR_LOGCOUNT, "LOGCOUNT_MONITOR");
			put(MONITOR_CORRELATION, "COLLECT_MONITOR");
			put(MONITOR_INTEGRATION, "INTEGRATION_MONITOR");
			put(PERFORMANCE, "PERFORMANCE");
			put(JOB, "JOB_MANAGEMENT");
			put(JOB_MST, "JOB_MANAGEMENT");
			put(JOB_KICK, "JOB_KICK");
			put(SYSYTEM_MAINTENANCE, "MAINTENANCE_NAME");
			put(SYSYTEM_SELFCHECK, "SELFCHECK_NAME");
			put(INFRA, "INFRA_MANAGEMENT");
			put(INFRA_FILE, "INFRA_FILE_MANAGER");
			put(REPORTING, "REPORTING");
			put(HINEMOS_MANAGER_MONITOR, "MNG");
			put(MONITOR_CUSTOMTRAP_N, "CUSTOMTRAP_MONITOR_N");
			put(MONITOR_CUSTOMTRAP_S, "CUSTOMTRAP_MONITOR_S");
			put(MONITOR_BINARYFILE_BIN, "BINARYFILE_MONITOR_BIN");
			put(MONITOR_PCAP_BIN, "PCAP_MONITOR_BIN");
		}});

		if(codeMap.containsKey(str)) {
			return codeMap.get(str);
		} else {
			if(!extensionTypeList.isEmpty()){
				for(ExtensionType extensionType: extensionTypeList){
					if(str.equals(extensionType.getTypeId())){
						return extensionType.getStringType();
					}
				}
			}
			return "";
		}
	}
}
