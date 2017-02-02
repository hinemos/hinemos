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

package com.clustercontrol.jobmanagement.bean;


/**
 * ジョブのジョブ変数におけるシステム変数に関する情報を定義するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.1.0
 */
public class SystemParameterConstant {
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

	//システム（ノード）
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
		PRIORITY
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
		CLOUD_LOCATION
	};

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
}