/*

Copyright (C) since 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.repository.bean;

/**
 *
 * NodePropertyの定数部分を切り出した物。
 *
 */
public class NodeConstant extends FacilityConstant{

	/** ----------------------- */
	/** ----- ファシリティ関連 ----- */
	/** ----------------------- */

	// FacilityConstantで定義

	/** ------------------------ */
	/** ----- ノード基本情報 ----- */
	/** ------------------------ */

	/** ----- HW ----- */
	/** プラットフォームファミリ名 */
	public static final String PLATFORM_FAMILY_NAME = "platformFamilyName";
	/** サブプラットフォームファミリ名 */
	public static final String SUB_PLATFORM_FAMILY_NAME = "subPlatformFamilyName";
	/** H/Wタイプ */
	public static final String HARDWARE_TYPE = "hardwareType";

	/** ----- IPアドレス関連 ----- */
	/** IPアドレスのバージョン */
	public static final String IP_ADDRESS_VERSION = "ipAddressVersion";
	/** IPv4のアドレス */
	public static final String IP_ADDRESS_V4 = "ipAddressV4";
	/** IPv6のアドレス */
	public static final String IP_ADDRESS_V6 = "ipAddressV6";
	/** ホスト名(複数設定可) */
	public static final String HOST_NAME = "hostName";

	/** ----- OS関連 ----- */
	/** ノード名 */
	public static final String NODE_NAME = "nodeName";
	/** OS名 */
	public static final String OS_NAME = "osName";
	/** OSリリース */
	public static final String OS_RELEASE = "osRelease";
	/** OSバージョン */
	public static final String OS_VERSION = "osVersion";
	/** 文字セット */
	public static final String CHARACTER_SET = "characterSet";

	/** ----- Hinemosエージェント関連 ----- */
	/** 即時反映用ポート番号 */
	public static final String AGENT_AWAKE_PORT = "agentAwakePort";

	/** ----- JOB ----- */
	/** ジョブ */
	public static final String JOB = "job";
	/** ジョブ優先度 */
	public static final String JOB_PRIORITY = "jobPriority";
	/** ジョブ多重度*/
	public static final String JOB_MULTIPLICITY = "jobMultiplicity";

	/** ------------------ */
	/** ----- サービス ----- */
	/** ------------------ */

	/** ----- SNMP関連 ----- */
	/** SNMP接続ユーザ */
	public static final String SNMP_USER = "snmpUser";
	/** SNMP接続認証パスワード */
	public static final String SNMP_AUTH_PASSWORD = "snmpAuthPassword";
	/** SNMP接続暗号化パスワード */
	public static final String SNMP_PRIV_PASSWORD = "snmpPrivPassword";
	/** SNMPポート番号 */
	public static final String SNMP_PORT = "snmpPort";
	/** SNMPコミュニティ名 */
	public static final String SNMP_COMMUNITY = "snmpCommunity";
	/** SNMPバージョン */
	public static final String SNMP_VERSION = "snmpVersion";
	/** SNMPセキュリティレベル */
	public static final String SNMP_SECURITY_LEVEL = "snmpSecurityLevel";
	/** SNMP認証プロトコル */
	public static final String SNMP_AUTH_PROTOCOL = "snmpAuthProtocol";
	/** SNMP暗号化プロトコル */
	public static final String SNMP_PRIV_PROTOCOL = "snmpPrivProtocol";
	/** SNMPポーリングタイムアウト */
	public static final String SNMPTIMEOUT = "snmpTimeout";
	/** SNMPポーリングリトライ回数 */
	public static final String SNMPRETRIES= "snmpRetries";

	/** ----- WBEM関連 ----- */
	/** WBEM接続ユーザ */
	public static final String WBEM_USER = "wbemUser";
	/** WBEM接続ユーザパスワード */
	public static final String WBEM_USER_PASSWORD = "wbemUserPassword";
	/** WBEM接続ポート番号 */
	public static final String WBEM_PORT = "wbemPort";
	/** WBEM接続プロトコル */
	public static final String WBEM_PROTOCOL = "wbemProtocol";
	/** WBEM接続タイムアウト*/
	public static final String WBEM_TIMEOUT = "wbemTimeout";
	/** WBEM接続リトライ回数 */
	public static final String WBEM_RETRIES = "wbemRetries";

	/** ----- IPMI関連 ----- */
	/** IPMI接続アドレス */
	public static final String IPMI_IP_ADDRESS = "ipmiIpAddress";
	/** IPMIポート番号 */
	public static final String IPMI_PORT = "ipmiPort";
	/** IPMI接続ユーザ */
	public static final String IPMI_USER = "ipmiUser";
	/** IPMI接続ユーザパスワード */
	public static final String IPMI_USER_PASSWORD = "ipmiUserPassword";
	/** IPMI接続タイムアウト*/
	public static final String IPMI_TIMEOUT = "ipmiTimeout";
	/** IPMI接続リトライ回数 */
	public static final String IPMI_RETRIES = "ipmiRetries";
	/** IPMI接続プロトコル */
	public static final String IPMI_PROTOCOL = "ipmiProtocol";
	/** IPMI特権レベル */
	public static final String IPMI_LEVEL = "ipmiLevel";

	/** ----- WinRM関連 ----- */
	/** WinRM接続ユーザ */
	public static final String WINRM_USER = "winrmUser";
	/** WinRM接続ユーザパスワード */
	public static final String WINRM_USER_PASSWORD = "winrmUserPassword";
	/** WinRMバージョン */
	public static final String WINRM_VERSION = "winrmVersion";
	/** WinRM接続ポート番号 */
	public static final String WINRM_PORT = "winrmPort";
	/** WinRM接続プロトコル */
	public static final String WINRM_PROTOCOL = "winrmProtocol";
	/** WinRM接続タイムアウト*/
	public static final String WINRM_TIMEOUT = "winrmTimeout";
	/** WinRM接続リトライ回数 */
	public static final String WINRM_RETRIES = "winrmRetries";

	/** ----- SSH関連 ----- */
	/** SSH接続ユーザ */
	public static final String SSH_USER = "sshUser";
	/** SSH接続ユーザパスワード */
	public static final String SSH_USER_PASSWORD = "sshUserPassword";
	/** SSH秘密鍵ファイル名 */
	public static final String SSH_PRIVATE_KEY_FILEPATH = "sshPrivateKeyFilepath";
	/** SSH秘密鍵パスフレーズ */
	public static final String SSH_PRIVATE_KEY_PASSPHRASE = "sshPrivateKeyPassphrase";
	/** SSHポート番号 */
	public static final String SSH_PORT = "sshPort";
	/** SSHタイムアウト */
	public static final String SSH_TIMEOUT = "sshTimeout";
	
	/** ------------------ */
	/** ----- デバイス ----- */
	/** ------------------ */

	/** ----- デバイス共通項目 ----- */
	/** デバイス種別 */
	public static final String DEVICE_TYPE = "deviceType";
	/** デバイス表示名 */
	public static final String DEVICE_DISPLAY_NAME = "deviceDisplayName";
	/** デバイスインデックス */
	public static final String DEVICE_INDEX = "deviceIndex";
	/** デバイス名 */
	public static final String DEVICE_NAME = "deviceName";
	/** デバイスサイズ */
	public static final String DEVICE_SIZE = "deviceSize";
	/** デバイスサイズ単位 */
	public static final String DEVICE_SIZE_UNIT = "deviceSizeUnit";
	/** デバイス説明 */
	public static final String DEVICE_DESCRIPTION = "deviceDescription";


	/** ----- デバイス項目(CPU) ----- */

	/** ----- デバイス項目(MEM) ----- */

	/** ----- デバイス項目(NIC) ----- */
	/** NIC IPアドレス */
	public static final String NIC_IP_ADDRESS = "nicIpAddress";
	/** NIC MACアドレス */
	public static final String NIC_MAC_ADDRESS = "nicMacAddress";

	/** ----- デバイス項目(DISK) ----- */
	/** DISK 回転数 */
	public static final String DISK_RPM = "diskRpm";


	/** ----- デバイス項目(ファイルシステム) ----- */
	/** ファイルシステム種別 */
	public static final String FILE_SYSTEM_TYPE = "fileSystemType"; // NFTS,FAT32,ext3,ext4,...


	/** ----------------- */
	/** ----- 仮想化 ----- */
	/** ----------------- */

	/** ----- クラウド・仮想化管理 ----- */
	/** クラウドサービス */
	public static final String CLOUDSERVICE= "cloudService";
	/** クラウドスコープ */
	public static final String CLOUDSCOPE= "cloudScope";
	/** クラウドリソースタイプ */
	public static final String CLOUDRESOURCETYPE= "cloudResourceType";
	/** クラウドリソース名 */
	public static final String CLOUDRESOURCENAME= "cloudResourceName";
	/** クラウドリソースID */
	public static final String CLOUDRESOURCEID= "cloudResourceId";
	/** クラウドロケーション */
	public static final String CLOUDLOCATION= "cloudLocation";

	/** -------------------- */
	/** ----- ノード変数 ----- */
	/** -------------------- */

	/** ----- ノード変数 ----- */
	/** 変数名 */
	public static final String NODE_VARIABLE_NAME = "nodeVariableName";
	/** 値 */
	public static final String NODE_VARIABLE_VALUE = "nodeVariableValue";

	/** ----------------- */
	/** ----- 保守 ----- */
	/** ----------------- */

	/** ----- 管理関連 ----- */
	/** 管理者 */
	public static final String ADMINISTRATOR = "administrator";
	/** 連絡先 */
	public static final String CONTACT = "contact";

	/** 備考 */
	public static final String NOTE= "note";

	/** ------------------------- */
	/** ----- 情報グループ種別 ----- */
	/** ------------------------- */

	/** ----- 基本情報 ----- */
	public static final String BASIC_INFORMATION = "basicInformation";
	/** HW */
	public static final String HARDWARE = "hardware";
	/** ネットワーク */
	public static final String NETWORK = "network";
	/** OS */
	public static final String OS = "os";
	/** Hinemosエージェント */
	public static final String AGENT = "agent";

	/** ----- サービス----- */
	public static final String SERVICE ="service";
	/** SNMP */
	public static final String SNMP ="snmp";
	/** WBEM */
	public static final String WBEM = "wbem";
	/** IPMI */
	public static final String IPMI = "ipmi";
	/** WinRM */
	public static final String WINRM = "winrm";
	/** SSH */
	public static final String SSH = "ssh";

	/** ----- デバイス----- */
	public static final String DEVICE = "device";
	/** 汎用デバイス */
	public static final String GENERAL_DEVICE = "generalDevice";
	public static final String GENERAL_DEVICE_LIST = "generalDeviceList";
	/** CPU */
	public static final String CPU = "cpu";
	public static final String CPU_LIST = "cpuList";
	/** メモリ */
	public static final String MEMORY = "memory";
	public static final String MEMORY_LIST = "memoryList";
	/** NIC */
	public static final String NETWORK_INTERFACE = "networkInterface";
	public static final String NETWORK_INTERFACE_LIST = "networkInterfaceList";
	/** DISK */
	public static final String DISK = "disk";
	public static final String DISK_LIST = "diskList";
	/** ファイルシステム */
	public static final String FILE_SYSTEM = "fileSystem";
	public static final String FILE_SYSTEM_LIST = "fileSystemList";

	/** ----- クラウド・仮想化----- */
	public static final String CLOUD_MANAGEMENT = "cloudManagement";

	/** ----- ノード変数----- */
	/** ノード変数 */
	public static final String NODE_VARIABLE = "nodeVariable";
	/** ノード変数 */
	public static final String GENERAL_NODE_VARIABLE = "generalNodeVariable";

	/** 保守 */
	public static final String MAINTENANCE = "maintenance";

}
