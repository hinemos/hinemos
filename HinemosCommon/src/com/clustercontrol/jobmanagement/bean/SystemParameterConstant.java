/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ジョブのジョブ変数におけるシステム変数に関する情報を定義するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.1.0
 */
public class SystemParameterConstant {
	/** エスケープした状態でのリプレイスを行わない場合のキー指定 */
	public static final String NOT_REPLACE_TO_ESCAPE = ":original";
	
	//システム（ジョブ）
	/** 呼び出し元のアプリケーション */
	public static final String APPLICATION = "APPLICATION";
	/** 呼び出し元のファシリティID */
	public static final String FACILITY_ID = "FACILITY_ID";
	/** ファイル名(実行契機がファイルチェックのときに値が入る) */
	public static final String FILENAME = "FILENAME";
	/** ディレクトリ(実行契機がファイルチェックのときに値が入る) */
	public static final String DIRECTORY = "DIRECTORY";
	/** 呼び出し元のメッセージ */
	public static final String MESSAGE = "MESSAGE";
	/** 呼び出し元の監視項目ID */
	public static final String MONITOR_ID = "MONITOR_ID";
	/** 呼び出し元の監視詳細 */
	public static final String MONITOR_DETAIL_ID = "MONITOR_DETAIL_ID";
	/** 呼び出し元のオリジナルメッセージ */
	public static final String ORG_MESSAGE = "ORG_MESSAGE";
	/** 呼び出し元のプラグインID */
	public static final String PLUGIN_ID = "PLUGIN_ID";
	/** 呼び出し元の重要度 */
	public static final String PRIORITY = "PRIORITY";
	/** ジョブセッションID */
	public static final String SESSION_ID = "SESSION_ID";
	/** ジョブセッション開始日時 */
	public static final String START_DATE = "START_DATE";
	/** ジョブ起動種類*/
	public static final String TRIGGER_INFO = "TRIGGER_INFO";
	/** ジョブ起動契機 */
	public static final String TRIGGER_TYPE = "TRIGGER_TYPE";
	/** 呼び出し元の終了値 */
	public static final String END_NUM = "END_NUM";
	/** ファイル名（ファイルチェックジョブ用） */
	public static final String FFILENAME = "FFILENAME";
	/** ディレクトリ（ファイルチェックジョブ用） */
	public static final String FDIRECTORY = "FDIRECTORY";
	/** ファイルチェックのチェック種別 */
	public static final String FCHECKCOND = "FCHECKCOND";
	/** 条件に一致したファイルのファイル更新日時 */
	public static final String FTIMESTAMP = "FTIMESTAMP";
	/** 条件に一致したファイルのファイルサイズ */
	public static final String FILESIZE = "FILESIZE";
	/** ファイルチェックが開始しているか */
	public static final String FCISSTART = "FCISSTART";

	//システム（ノード）
	/** 全件取得 */
	public static final String DATA_ALL = "ALL";
	/** 先頭1件のみ取得 */
	public static final String DATA_TOP = "TOP";
	/** 置換キー区切り文字 */
	public static final String KEY_SEPARATOR = ":";
	
	public static final String FACILITY_NAME = "FACILITY_NAME";
	public static final String IP_ADDRESS_VERSION = "IP_ADDRESS_VERSION";
	public static final String IP_ADDRESS = "IP_ADDRESS";
	public static final String IP_ADDRESS_V4 = "IP_ADDRESS_V4";
	public static final String IP_ADDRESS_V6 = "IP_ADDRESS_V6";
	public static final String NODE_NAME = "NODE_NAME";
	public static final String OS_NAME = "OS_NAME";
	public static final String OS_RELEASE = "OS_RELEASE";
	public static final String OS_VERSION = "OS_VERSION";
	public static final String CHARSET = "CHARSET";
	public static final String AGENT_AWAKE_PORT = "AGENT_AWAKE_PORT";
	public static final String JOB_PRIORITY = "JOB_PRIORITY";
	public static final String JOB_MULTIPLICITY = "JOB_MULTIPLICITY";
	public static final String SNMP_PORT = "SNMP_PORT";
	public static final String SNMP_COMMUNITY = "SNMP_COMMUNITY";
	public static final String SNMP_VERSION = "SNMP_VERSION";
	public static final String SNMP_TIMEOUT = "SNMP_TIMEOUT";
	public static final String SNMP_TRIES = "SNMP_TRIES";
	public static final String WBEM_PORT = "WBEM_PORT";
	public static final String WBEM_PROTOCOL = "WBEM_PROTOCOL";
	public static final String WBEM_TIMEOUT = "WBEM_TIMEOUT";
	public static final String WBEM_TRIES = "WBEM_TRIES";
	public static final String WBEM_USER = "WBEM_USER";
	public static final String WBEM_PASSWORD = "WBEM_PASSWORD";
	public static final String WINRM_USER = "WINRM_USER";
	public static final String WINRM_PASSWORD = "WINRM_PASSWORD";
	public static final String WINRM_VERSION = "WINRM_VERSION";
	public static final String WINRM_PORT = "WINRM_PORT";
	public static final String WINRM_PROTOCOL = "WINRM_PROTOCOL";
	public static final String WINRM_TIMEOUT = "WINRM_TIMEOUT";
	public static final String WINRM_TRIES = "WINRM_TRIES";
	public static final String IPMI_IP_ADDRESS = "IPMI_IP_ADDRESS";
	public static final String IPMI_PORT = "IPMI_PORT";
	public static final String IPMI_TIMEOUT = "IPMI_TIMEOUT";
	public static final String IPMI_TRIES = "IPMI_TRIES";
	public static final String IPMI_PROTOCOL = "IPMI_PROTOCOL";
	public static final String IPMI_LEVEL = "IPMI_LEVEL";
	public static final String IPMI_USER = "IPMI_USER";
	public static final String IPMI_PASSWORD = "IPMI_PASSWORD";
	public static final String SSH_USER = "SSH_USER";
	public static final String SSH_USER_PASSWORD = "SSH_USER_PASSWORD";
	public static final String SSH_PRIVATE_KEY_FILENAME = "SSH_PRIVATE_KEY_FILENAME";
	public static final String SSH_PRIVATE_KEY_PASSPHRASE = "SSH_PRIVATE_KEY_PASSPHRASE";
	public static final String SSH_PORT = "SSH_PORT";
	public static final String SSH_TIMEOUT = "SSH_TIMEOUT";
	public static final String CLOUD_SERVICE = "CLOUD_SERVICE";
	public static final String CLOUD_SCOPE = "CLOUD_SCOPE";
	public static final String CLOUD_RESOURCE_TYPE = "CLOUD_RESOURCE_TYPE";
	public static final String CLOUD_RESOURCE_ID = "CLOUD_RESOURCE_ID";
	public static final String CLOUD_RESOURCE_NAME = "CLOUD_RESOURCE_NAME";
	public static final String CLOUD_LOCATION = "CLOUD_LOCATION"; 
	public static final String CLOUD_LOG_PRIORITY = "CLOUD_LOG_PRIORITY"; 
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String RPA_LOG_DIRECTORY = "RPA_LOG_DIRECTORY";
	public static final String RPA_MGMT_TOOL_TYPE = "RPA_MGMT_TOOL_TYPE";
	public static final String RPA_RESOURCE_ID = "RPA_RESOURCE_ID";
	public static final String RPA_USER = "RPA_USER";
	public static final String RPA_EXEC_ENV_ID = "RPA_EXEC_ENV_ID";
	public static final String RPA_TOOL_EXE_FILEPATH = "RPA_TOOL_EXE_FILEPATH";
	public static final String RPA_TOOL_EXE_FILENAME = "RPA_TOOL_EXE_FILENAME";
	public static final String RPA_TOOL_SCENARIO_FILEPATH = "RPA_TOOL_SCENARIO_FILEPATH";
	public static final String RPA_TOOL_OPTIONS = "RPA_TOOL_OPTIONS";
	public static final String AUTO_DEVICE_SEARCH = "AUTO_DEVICE_SEARCH";
	public static final String PLATFORM_FAMILY = "PLATFORM_FAMILY";
	public static final String SUB_PLATFORM_FAMILY = "SUB_PLATFORM_FAMILY";
	public static final String HARDWARE_TYPE = "HARDWARE_TYPE";
	public static final String HOSTNAME = "HOSTNAME";
	public static final String OS_STARTUP = "OS_STARTUP";
	public static final String CPU = "CPU";
	public static final String MEMORY = "MEMORY";
	public static final String NIC = "NIC";
	public static final String DISK = "DISK";
	public static final String FILE_SYSTEM = "FILE_SYSTEM";
	public static final String DEVICE = "DEVICE";
	public static final String NET_STAT = "NET_STAT";
	public static final String PROCESS= "PROCESS";
	public static final String PACKAGE= "PACKAGE";
	public static final String PRODUCT= "PRODUCT";
	public static final String LICENSE= "LICENSE";
	public static final String CUSTOM= "CUSTOM";
	public static final String SNMP_USER = "SNMP_USER";
	public static final String SNMP_SECURITY_LEVEL = "SNMP_SECURITY_LEVEL";
	public static final String SNMP_AUTH_PASSWORD = "SNMP_AUTH_PASSWORD";
	public static final String SNMP_PRIV_PASSWORD = "SNMP_PRIV_PASSWORD";
	public static final String SNMP_AUTH_PROTOCOL = "SNMP_AUTH_PROTOCOL";
	public static final String SNMP_PRIV_PROTOCOL = "SNMP_PRIV_PROTOCOL";
	public static final String ADMINISTRATOR = "ADMINISTRATOR";
	public static final String CONTACT = "CONTACT";
	public static final String CREATE_USER_ID = "CREATE_USER_ID";
	public static final String CREATE_DATETIME = "CREATE_DATETIME";
	public static final String MODIFY_USER_ID = "MODIFY_USER_ID";
	public static final String MODIFY_DATETIME = "MODIFY_DATETIME";
	public static final String NOTE = "NOTE";


	/** ジョブ変数 ヘッダー */
	public static final String PREFIX = "#[";
	/** ジョブ変数 フッター */
	public static final String SUFFIX = "]";

	// ジョブパラメータ情報から取得
	public static final String SYSTEM_ID_LIST_JOB_PARAM[] = {
		APPLICATION,
		FACILITY_ID,
		FILENAME,
		DIRECTORY,
		MESSAGE,
		MONITOR_ID,
		MONITOR_DETAIL_ID,
		ORG_MESSAGE,
		PLUGIN_ID,
		PRIORITY,
		END_NUM
	};

	// ジョブパラメータ情報(通知情報)
	public static final String SYSTEM_ID_LIST_NOTIFY_PARAM[] = {
		APPLICATION,
		FACILITY_ID,
		MESSAGE,
		MONITOR_ID,
		MONITOR_DETAIL_ID,
		ORG_MESSAGE,
		PLUGIN_ID,
		PRIORITY
	};
	
	// ジョブパラメータ情報(ジョブ実行情報)
	public static final String SYSTEM_ID_LIST_RUN_JOB_PARAM[] = {
		FILENAME,
		DIRECTORY,
	};
	
	// ジョブセッション情報から取得
	public static final String SYSTEM_ID_LIST_JOB_SESSION[] = {
		SESSION_ID,
		START_DATE,
		TRIGGER_INFO,
		TRIGGER_TYPE
	};

	// ノード情報から取得
	public static final String SYSTEM_ID_LIST_NODE_INFO [] = {
		FACILITY_NAME,
		IP_ADDRESS_VERSION,
		IP_ADDRESS,
		IP_ADDRESS_V4,
		IP_ADDRESS_V6,
		NODE_NAME,
		OS_NAME,
		OS_RELEASE,
		OS_VERSION,
		CHARSET,
		AGENT_AWAKE_PORT,
		JOB_PRIORITY,
		JOB_MULTIPLICITY,
		SNMP_PORT,
		SNMP_COMMUNITY,
		SNMP_VERSION,
		SNMP_TIMEOUT,
		SNMP_TRIES,
		WBEM_PORT,
		WBEM_PROTOCOL,
		WBEM_TIMEOUT,
		WBEM_TRIES,
		WBEM_USER,
		WBEM_PASSWORD,
		WINRM_USER,
		WINRM_PASSWORD,
		WINRM_VERSION,
		WINRM_PORT,
		WINRM_PROTOCOL,
		WINRM_TIMEOUT,
		WINRM_TRIES,
		IPMI_IP_ADDRESS,
		IPMI_PORT,
		IPMI_TIMEOUT,
		IPMI_TRIES,
		IPMI_PROTOCOL,
		IPMI_LEVEL,
		IPMI_USER,
		IPMI_PASSWORD,
		SSH_USER,
		SSH_USER_PASSWORD,
		SSH_PRIVATE_KEY_FILENAME,
		SSH_PRIVATE_KEY_PASSPHRASE,
		SSH_PORT,
		SSH_TIMEOUT,
		CLOUD_SERVICE,
		CLOUD_SCOPE,
		CLOUD_RESOURCE_TYPE,
		CLOUD_RESOURCE_ID,
		CLOUD_RESOURCE_NAME,
		CLOUD_LOCATION,
		CLOUD_LOG_PRIORITY,
		DESCRIPTION,
		AUTO_DEVICE_SEARCH,
		PLATFORM_FAMILY,
		SUB_PLATFORM_FAMILY,
		HARDWARE_TYPE,
		HOSTNAME,
		OS_STARTUP,
		CPU,
		MEMORY,
		NIC,
		DISK,
		FILE_SYSTEM,
		DEVICE,
		NET_STAT,
		PROCESS,
		PACKAGE,
		PRODUCT,
		LICENSE,
		CUSTOM,
		SNMP_USER,
		SNMP_SECURITY_LEVEL,
		SNMP_AUTH_PASSWORD,
		SNMP_PRIV_PASSWORD,
		SNMP_AUTH_PROTOCOL,
		SNMP_PRIV_PROTOCOL,
		ADMINISTRATOR,
		CONTACT,
		CREATE_USER_ID,
		CREATE_DATETIME,
		MODIFY_USER_ID,
		MODIFY_DATETIME,
		NOTE
	};

	// ジョブパラメータ情報(ファイルチェックジョブ)
	public static final String SYSTEM_ID_LIST_FILECHECK_JOB_PARAM[] = {
		FFILENAME,
		FDIRECTORY,
		FCHECKCOND,
		FTIMESTAMP,
		FILESIZE,
		FCISSTART
	};

	private static List<String> notifyParamIdList =
			Collections.unmodifiableList(Arrays.asList(SystemParameterConstant.SYSTEM_ID_LIST_NOTIFY_PARAM));
	
	private static List<String> runJobParamIdList =
			Collections.unmodifiableList(Arrays.asList(SystemParameterConstant.SYSTEM_ID_LIST_RUN_JOB_PARAM));

	private static List<String> filecheckJobParamIdList =
			Collections.unmodifiableList(Arrays.asList(SystemParameterConstant.SYSTEM_ID_LIST_FILECHECK_JOB_PARAM));

	/**
	 * strが#[param]の形式であるかを判定する
	 *
	 * @param str
	 * @param param
	 * @return
	 */
	public static boolean isParam(String str, String param){
		if(str == null || param == null){
			return false;
		}
		if(str.equals(getParamText(param))){
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * ジョブ変数をパラメータ形式で返却する
	 *
	 * @param param
	 * @return
	 */
	public static String getParamText(String param){
		return PREFIX + param + SUFFIX;
	}

	/**
	 * ジョブ変数（パラメータ形式）からパラメータIDを返却する
	 *
	 * @param paramText
	 * @return
	 */
	public static String getParamId(String paramText){
		return paramText.substring(PREFIX.length(), paramText.length()-SUFFIX.length());
	}
	
	public static boolean isNofityParam(String paramId) {
		
		if (notifyParamIdList.contains(paramId)) {
			return true;
		}
		return false;
	}
	
	public static boolean isNofityOrgParam(String paramId) {
		
		if (notifyParamIdList.contains(getNotOriginalParam(paramId))) {
			return true;
		}
		return false;
	}
	
	public static boolean isRunJobParam(String paramId) {
		
		if (runJobParamIdList.contains(paramId)) {
			return true;
		}
		return false;
	}
	
	public static boolean isRunJobOrgParam(String paramId) {
		
		if (runJobParamIdList.contains(getNotOriginalParam(paramId))) {
			return true;
		}
		return false;
	}

	/**
	 * 「:jobid」を除いた形でファイルチェックジョブのジョブ変数であるかを判定する
	 * 
	 * @param paramId
	 * @return
	 */
	public static boolean isFilecheckJobParam(String paramId) {
		if (!paramId.contains(KEY_SEPARATOR)) {
			return false;
		}
		String prefix = paramId.substring(0, paramId.indexOf(KEY_SEPARATOR));
		if (filecheckJobParamIdList.contains(prefix)) {
			return true;
		}
		return false;
	}

	/**
	 * strが#[param]の形式であるかを判定する
	 *
	 * @param str
	 * @param param
	 * @return
	 */
	public static boolean isParamFormat(String str) {
		if (str == null) {
			return false;
		}
		return str.startsWith(SystemParameterConstant.PREFIX)
				&& str.endsWith(SystemParameterConstant.SUFFIX);
	}
	
	public static String getNotOriginalParam(String paramId) {
		int escapeLength = SystemParameterConstant.NOT_REPLACE_TO_ESCAPE.length();
		int paramIdLegnth = paramId.length();
		
		if (paramIdLegnth < escapeLength) {
			return null;
		}
		
		int paramIdOrgIdx = paramIdLegnth - escapeLength;
		if (!SystemParameterConstant.NOT_REPLACE_TO_ESCAPE.equals(paramId.substring(paramIdOrgIdx))) {
			return null;
		}
		return paramId.substring(0, paramIdOrgIdx);
	}
}