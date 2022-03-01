/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Hinemosの機能を定数として格納するクラス<BR>
 *
 * @version 6.2.0
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
	/** コマンド通知テンプレート*/
	public static final String PLATFORM_COMMAND_TEMPLATE = "PLT_CMD_TMP";
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
	/** リポジトリ[自動登録] */
	public static final String PLATFORM_REPSITORY_AUTO_REGISTER = "PLT_REP_AREG";
	/** RESTアクセス */
	public static final String PLATFORM_REST_ACCESS = "PLT_RES_ACS";

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
	/** RPA管理ツールサービス監視 */
	public static final String MONITOR_RPA_MGMT_TOOL_SERVICE = "MON_RPA_MGMT_TOOL_SERVICE_B";
	/** RPAログファイル監視 */
	public static final String MONITOR_RPA_LOGFILE = "MON_RPA_LOGFILE_S";
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
	/** クラウドサービス監視 */
	public static final String MONITOR_CLOUD_SERVICE_CONDITION = "MON_CLOUD_SERVICE_CONDITION";
	/** クラウド課金監視 */
	public static final String MONITOR_CLOUD_SERVICE_BILLING = "MON_CLOUD_SERVICE_BILLING";
	/** クラウド課金詳細監視 */
	public static final String MONITOR_CLOUD_SERVICE_BILLING_DETAIL = "MON_CLOUD_SERVICE_BILLING_DETAIL";
	/** クラウドログ監視 */
	public static final String MONITOR_CLOUD_LOG = "MON_CLOUD_LOG";
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
	/** ジョブ管理 ジョブ実行契機 */
	public static final String JOB_KICK  = "JOB_KICK";
	/** ジョブ管理:同時実行制御キュー */
	public static final String JOB_QUEUE  = "JOB_QUEUE";
	/** ジョブ管理 ジョブマップ */
	public static final String JOBMAP_IMAGE_FILE  = "JOBMAP_IMAGE_FILE";
	/** ジョブ管理 ジョブ連携送信設定 */
	public static final String JOB_LINK_SEND  = "JOB_LINK_SEND";
	/** ジョブ管理 ジョブ-開始 */
	public static final String JOB_START = "JOB_START";
	/** ジョブ管理 ジョブ-終了 */
	public static final String JOB_END = "JOB_END";
	/** ジョブ管理 ジョブ-開始遅延 */
	public static final String JOB_START_DELAY = "JOB_START_DELAY";
	/** ジョブ管理 ジョブ-終了遅延 */
	public static final String JOB_END_DELAY = "JOB_END_DELAY";
	/** ジョブ管理 ジョブ-同時実行制御待ち開始 */
	public static final String JOB_QUEUE_START = "JOB_QUEUE_START";
	/** ジョブ管理 ジョブ-同時実行待ち終了 */
	public static final String JOB_QUEUE_END = "JOB_QUEUE_END";
	/** ジョブ管理 ジョブ-多重度超過 */
	public static final String JOB_EXCEEDED_MULTIPLICITY = "JOB_EXCEEDED_MULTIPLICITY";

	/** 自動デバイスサーチ */
	public static final String REPOSITORY_DEVICE_SEARCH = "REP_DS";

	/** 対象構成情報 */
	public static final String NODE_CONFIG_SETTING = "NODE_CONFIG_SETTING";

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
	public static final String INFRA_RUN_START = "RUN_START";
	public static final String INFRA_RUN_END = "RUN_END";
	public static final String INFRA_CHECK_START = "CHECK_START";
	public static final String INFRA_CHECK_END = "CHECK_END";

	/** レポーティング */
	public static final String REPORTING = "REPORTING";

	/** REST-API*/
	public static final String REST_API = "REST_API";

	/** RPA管理 */
	public static final String RPA = "RPA";
	public static final String RPA_ACCOUNT = "RPA_ACCOUNT";
	public static final String RPA_SCENARIO_TAG = "RPA_SCENARIO_TAG";
	public static final String RPA_SCENARIO = "RPA_SCENARIO";
	public static final String RPA_SCENARIO_CREATE = "RPA_SCENARIO_CREATE";
	public static final String RPA_SCENARIO_CORRECT = "RPA_SCENARIO_CORRECT";
	
	/** クラウド仮想化管理*/
	public static final String XCLOUD = "CLOUD";
	/** クラウドサービス監視*/
	public static final String CLOUD_MONITOR_SERVICE = "MON_CLOUD_SERVICE_CONDITION";

	/** マルチテナント制御 */
	public static final String MULTI_TENANT = "MULTI_TENANT";

	/** フィルタ設定 */
	public static final String FILTER_SETTING = "FILTER_SETTING";

	/** SDML制御設定 */
	public static final String SDML_CONTROL = "SDML_CONTROL";

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

		if(typeId.equals(PLATFORM) ||
				typeId.equals(PLATFORM_NOTIFY) ||
				typeId.equals(PLATFORM_MAIL_TEMPLATE) ||
				typeId.equals(PLATFORM_CALENDAR) ||
				typeId.equals(PLATFORM_CALENDAR_PATTERN) ||
				typeId.equals(PLATFORM_ACCESS) ||
				typeId.equals(PLATFORM_PRIORITY_JUDGMENT) ||
				typeId.equals(PLATFORM_REPOSITORY) ||
				typeId.equals(PLATFORM_REPSITORY_NODE) ||
				typeId.equals(PLATFORM_REPSITORY_SCOPE) ||
				typeId.equals(PLATFORM_REPSITORY_AUTO_REGISTER) ||
				typeId.equals(MONITOR) ||
				typeId.equals(MONITOR_AGENT ) ||
				typeId.equals(MONITOR_CUSTOM_N) ||
				typeId.equals(MONITOR_CUSTOM_S) ||
				typeId.equals(MONITOR_HTTP_N) ||
				typeId.equals(MONITOR_HTTP_S) ||
				typeId.equals(MONITOR_HTTP_SCENARIO) ||
				typeId.equals(MONITOR_LOGFILE) ||
				typeId.equals(MONITOR_LOGCOUNT) ||
				typeId.equals(MONITOR_PERFORMANCE) ||
				typeId.equals(MONITOR_PING) ||
				typeId.equals(MONITOR_PORT ) ||
				typeId.equals(MONITOR_PROCESS) ||
				typeId.equals(MONITOR_SNMP_N ) ||
				typeId.equals(MONITOR_SNMP_S ) ||
				typeId.equals(MONITOR_SNMPTRAP) ||
				typeId.equals(MONITOR_SQL_N ) ||
				typeId.equals(MONITOR_SQL_S ) ||
				typeId.equals(MONITOR_SYSTEMLOG) ||
				typeId.equals(MONITOR_SCOPE) ||
				typeId.equals(MONITOR_STATUS) ||
				typeId.equals(MONITOR_EVENT) ||
				typeId.equals(MONITOR_WINSERVICE) ||
				typeId.equals(MONITOR_WINEVENT) ||
				typeId.equals(MONITOR_JMX) ||
				typeId.equals(MONITOR_CUSTOMTRAP_N) ||
				typeId.equals(MONITOR_CUSTOMTRAP_S) ||
				typeId.equals(MONITOR_CORRELATION) ||
				typeId.equals(MONITOR_INTEGRATION) ||
				typeId.equals(MONITOR_BINARYFILE_BIN) ||
				typeId.equals(MONITOR_PCAP_BIN) ||
				typeId.equals(MONITOR_RPA_MGMT_TOOL_SERVICE) ||
				typeId.equals(MONITOR_RPA_LOGFILE) ||
				typeId.equals(NODE_CONFIG_SETTING) ||
				typeId.equals(HUB_LOGFORMAT) ||
				typeId.equals(HUB_TRANSFER) ||
				typeId.equals(PERFORMANCE) ||
				typeId.equals(PERFORMANCE_RECORD) ||
				typeId.equals(PERFORMANCE_REALTIME) ||
				typeId.equals(JOB ) ||
				typeId.equals(JOB_MST ) ||
				typeId.equals(JOB_SESSION) ||
				typeId.equals(JOB_SESSION_DETAIL) ||
				typeId.equals(JOB_SESSION_NODE) ||
				typeId.equals(JOB_KICK) ||
				typeId.equals(JOB_QUEUE) ||
				typeId.equals(JOB_SCHEDULE_RUN_DETAIL) ||
				typeId.equals(JOB_SESSION_FILE ) ||
				typeId.equals(SDML_CONTROL) ||
				typeId.equals(SYSYTEM ) ||
				typeId.equals(SYSYTEM_MAINTENANCE) ||
				typeId.equals(SYSYTEM_SELFCHECK) ||
				typeId.equals(INFRA) ||
				typeId.equals(INFRA_FILE) ||
				typeId.equals(REPORTING) ||
				typeId.equals(REST_API) ||
				typeId.equals(RPA_ACCOUNT) ||
				typeId.equals(RPA_SCENARIO_TAG) ||
				typeId.equals(RPA_SCENARIO) ||
				typeId.equals(XCLOUD) ||
				typeId.equals(MULTI_TENANT) ||
				typeId.equals(FILTER_SETTING)) {

			return true;
		} else if(!extensionTypeList.isEmpty()){
			for(ExtensionType extensionType: extensionTypeList){
				if(typeId.equals(extensionType.getTypeId())){
					return true;
				}
			}
		}

		return false;
	}

	public static String nameToMessageCode(String string) {
		if (string.equals(PLATFORM_ACCESS)) {
			return "ACCESSCONTROL";
		} else if (string.equals(PLATFORM_PRIORITY_JUDGMENT)) {
			return "PRIORITY_JUDGMENT";
		} else if (string.equals(PLATFORM_CALENDAR)) {
			return "CALENDAR";
		} else if (string.equals(PLATFORM_CALENDAR_PATTERN)) {
			return "CALENDAR_PATTERN";
		} else if (string.equals(PLATFORM_REPOSITORY)
				|| string.equals(PLATFORM_REPSITORY_AUTO_REGISTER)) {
			return "REPOSITORY";
		} else if (string.equals(PLATFORM_NOTIFY)) {
			return "NOTIFY";
		} else if (string.equals(PLATFORM_MAIL_TEMPLATE)) {
			return "MAIL_TEMPLATE";
		} else if (string.equals(PLATFORM_COMMAND_TEMPLATE)) {
			return "COMMAND_TEMPLATE";
		} else if (string.equals(PLATFORM_REST_ACCESS)) {
			return "REST_ACCESS_INFO";
		} else if (string.equals(MONITOR)) {
			return "MONITOR_SETTING";
		} else if (string.equals(MONITOR_AGENT)) {
			return "AGENT_MONITOR";
		} else if (string.equals(MONITOR_CUSTOM_N)) {
			return "CUSTOM_MONITOR_N";
		} else if (string.equals(MONITOR_CUSTOM_S)) {
			return "CUSTOM_MONITOR_S";
		} else if (string.equals(MONITOR_HTTP_N)) {
			return "HTTP_MONITOR_N";
		} else if (string.equals(MONITOR_HTTP_S)) {
			return "HTTP_MONITOR_S";
		} else if (string.equals(MONITOR_HTTP_SCENARIO)) {
			return "HTTP_MONITOR_SCENARIO";
		} else if (string.equals(MONITOR_PING)) {
			return "PING_MONITOR";
		} else if (string.equals(MONITOR_PROCESS)) {
			return "PROCESS_MONITOR";
		} else if (string.equals(MONITOR_SNMP_N)) {
			return "SNMP_MONITOR_N";
		} else if (string.equals(MONITOR_SNMP_S)) {
			return "SNMP_MONITOR_S";
		} else if (string.equals(MONITOR_SNMPTRAP)) {
			return "SNMPTRAP_MONITOR";
		} else if (string.equals(MONITOR_SQL_N)) {
			return "SQL_MONITOR_N";
		} else if (string.equals(MONITOR_SQL_S)) {
			return "SQL_MONITOR_S";
		} else if (string.equals(MONITOR_PERFORMANCE)) {
			return "PERFORMANCE_MONITOR";
		} else if (string.equals(MONITOR_PORT)) {
			return "PORT_MONITOR";
		} else if (string.equals(MONITOR_WINSERVICE)) {
			return "WINSERVICE_MONITOR";
		} else if (string.equals(MONITOR_WINEVENT)) {
			return "WINEVENT_MONITOR";
		} else if (string.equals(MONITOR_JMX)) {
			return "JMX_MONITOR";
		} else if (string.equals(HUB_LOGFORMAT)) {
			return "LOG_FORMAT";
		} else if (string.equals(HUB_TRANSFER)) {
			return "LOG_TRANSFER";
		} else if (string.equals(MONITOR_SYSTEMLOG)) {
			return "SYSTEMLOG_MONITOR";
		} else if (string.equals(MONITOR_LOGFILE)) {
			return "LOGFILE_MONITOR";
		} else if (string.equals(MONITOR_LOGCOUNT)) {
			return "LOGCOUNT_MONITOR";
		} else if (string.equals(MONITOR_CORRELATION)) {
			return "COLLECT_MONITOR";
		} else if (string.equals(MONITOR_INTEGRATION)) {
			return "INTEGRATION_MONITOR";
		} else if (string.equals(MONITOR_RPA_LOGFILE)) {
			return "RPA_LOGFILE_MONITOR";
		} else if (string.equals(MONITOR_RPA_MGMT_TOOL_SERVICE)) {
			return "RPA_MANAGEMENT_TOOL_SERVICE_MONITOR";
		} else if (string.equals(PERFORMANCE)) {
			return "PERFORMANCE";
		} else if (string.equals(JOB)) {
			return "JOB_MANAGEMENT";
		} else if (string.equals(JOB_MST)) {
			return "JOB_MANAGEMENT";
		} else if (string.equals(JOB_KICK)) {
			return "JOB_KICK";
		} else if (string.equals(JOB_QUEUE)) {
			return "JOB_QUEUE";
		} else if (string.equals(JOB_LINK_SEND)) {
			return "JOBLINK_SEND_SETTING";
		} else if (string.equals(JOBMAP_IMAGE_FILE)) {
			return "JOBMAP_ICON_IMAGE";
		} else if (string.equals(SYSYTEM_MAINTENANCE)) {
			return "MAINTENANCE_NAME";
		} else if (string.equals(SYSYTEM_SELFCHECK)) {
			return "SELFCHECK_NAME";
		} else if (string.equals(INFRA)) {
			return "INFRA_MANAGEMENT";
		} else if (string.equals(INFRA_FILE)) {
			return "INFRA_FILE_MANAGER";
		} else if (string.equals(REPORTING)) {
			return "REPORTING";
		} else if (string.equals(HINEMOS_MANAGER_MONITOR)) {
			return "MNG";
		} else if (string.equals(MONITOR_CUSTOMTRAP_N)) {
			return "CUSTOMTRAP_MONITOR_N";
		} else if (string.equals(MONITOR_CUSTOMTRAP_S)){
			return "CUSTOMTRAP_MONITOR_S";
		} else if (string.equals(MONITOR_BINARYFILE_BIN)){
			return "BINARYFILE_MONITOR_BIN";
		} else if (string.equals(MONITOR_PCAP_BIN)){
			return "PCAP_MONITOR_BIN";
		} else if (string.equals(NODE_CONFIG_SETTING)) {
			return "NODE_CONFIG_SETTING";
		}  else if (string.equals(REST_API)) {
			return "REST_API";
		} else if (string.equals(RPA_SCENARIO)){
			return "RPA_SCENARIO";
		} else if (string.equals(RPA_SCENARIO_CREATE)){
			return "RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING";
		} else if (string.equals(RPA_SCENARIO_CORRECT)){
			return "RPA_SCENARIO_OPERATION_RESULT_UPDATE_APPLICATION";
		} else if (string.equals(XCLOUD)){
			return "XCLOUD";
		} else if (string.equals(SDML_CONTROL)) {
			return "SDML_CONTROL_SETTING";
		}  else if (string.equals(REST_API)) {
		} else if (string.equals(MULTI_TENANT)){
			return "MULTI_TENANT_CONTROL";
		} else if (string.equals(FILTER_SETTING)) {
			return "FILTER_SETTING";
		} else if (string.equals(RPA)){
			return "RPA";
		} else if(!extensionTypeList.isEmpty()){
			for(ExtensionType extensionType: extensionTypeList){
				if(string.equals(extensionType.getTypeId())){
					return extensionType.getStringType();
				}
			}
		}
		return "";
	}
}
