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

package com.clustercontrol.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Hinemosの機能を定数として格納するクラス<BR>
 *
 * @version 6.0.0
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
	/** Windowsサービス監視 */
	public static final String MONITOR_WINSERVICE = "MON_WINSERVICE_B";
	/** Windowsイベント監視 */
	public static final String MONITOR_WINEVENT = "MON_WINEVENT_S";
	/** カスタムトラップ監視 （数値）*/
	public static final String MONITOR_CUSTOMTRAP_N = "MON_CUSTOMTRAP_N";
	/** カスタムトラップ監視 （文字列） */
	public static final String MONITOR_CUSTOMTRAP_S = "MON_CUSTOMTRAP_S";	
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
				typeId.equals(MONITOR) ||
				typeId.equals(MONITOR_AGENT ) ||
				typeId.equals(MONITOR_CUSTOM_N) ||
				typeId.equals(MONITOR_CUSTOM_S) ||
				typeId.equals(MONITOR_HTTP_N) ||
				typeId.equals(MONITOR_HTTP_S) ||
				typeId.equals(MONITOR_HTTP_SCENARIO) ||
				typeId.equals(MONITOR_LOGFILE) ||
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
				typeId.equals(JOB_SCHEDULE_RUN_DETAIL) ||
				typeId.equals(JOB_SESSION_FILE ) ||
				typeId.equals(SYSYTEM ) ||
				typeId.equals(SYSYTEM_MAINTENANCE) ||
				typeId.equals(SYSYTEM_SELFCHECK) ||
				typeId.equals(INFRA) ||
				typeId.equals(INFRA_FILE) ||
				typeId.equals(REPORTING)){

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
		} else if (string.equals(PLATFORM_REPOSITORY)) {
			return "REPOSITORY";
		} else if (string.equals(PLATFORM_NOTIFY)) {
			return "NOTIFY";
		} else if (string.equals(PLATFORM_MAIL_TEMPLATE)) {
			return "MAIL_TEMPLATE";
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
		} else if (string.equals(PERFORMANCE)) {
			return "PERFORMANCE";
		} else if (string.equals(JOB)) {
			return "JOB_MANAGEMENT";
		} else if (string.equals(JOB_MST)) {
			return "JOB_MANAGEMENT";
		} else if (string.equals(JOB_KICK)) {
			return "JOB_KICK";
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
