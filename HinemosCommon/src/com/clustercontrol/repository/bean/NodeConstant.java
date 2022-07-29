/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
	/** ノード名 */
	public static final String NODE_NAME = "nodeName";

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
	/** --- RPAツール情報 -- */
	/** ------------------ */

	/** ログ出力先ディレクトリ */
	public static final String RPA_LOG_DIRECTORY = "rpaLogDir";

	/** ------------------ */
	/** - RPA管理ツール情報 - */
	/** ------------------ */

	/** RPA管理ツールタイプ */
	public static final String RPA_MANAGEMENT_TOOL_TYPE = "rpaManagementToolType";
	/** RPAリソースID */
	public static final String RPA_RESOURCE_ID = "rpaResourceId";
	/** ユーザ名 */
	public static final String RPA_USER = "rpaUser";
	/** RPA実行環境ID */
	public static final String RPA_EXECUTION_ENVIRONMENT_ID = "rpaExecEnvId";

	
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
	/** ----- 構成情報 ----- */
	/** ------------------ */

	/** ----- OS関連 ----- */
	/** OS名 */
	public static final String OS_NAME = "osName";
	/** OSリリース */
	public static final String OS_RELEASE = "osRelease";
	/** OSバージョン */
	public static final String OS_VERSION = "osVersion";
	/** 文字セット */
	public static final String CHARACTER_SET = "characterSet";
	/** 起動日時 */
	public static final String OS_STARTUP_DATE_TIME = "osStartupDateTime";

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
	/** コア数 */
	public static final String CPU_CORE_COUNT = "cpuCoreCount";
	/** スレッド数 */
	public static final String CPU_THREAD_COUNT = "cpuThreadCount";
	/** クロック数 */
	public static final String CPU_CLOCK_COUNT = "cpuClockCount";

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


	/** -------------------- */
	/** ----- ネットワーク接続 ----- */
	/** -------------------- */

	/** ----- ネットワーク接続 ----- */
	/** プロトコル */
	public static final String NETSTAT_PROTOCOL = "netstatProtocol";
	/** ローカルIPアドレス */
	public static final String NETSTAT_LOCAL_IP_ADDRESS = "netstatLocalIpAddress";
	/** ローカルポート */
	public static final String NETSTAT_LOCAL_PORT = "netstatLocalPort";
	/** 外部IPアドレス */
	public static final String NETSTAT_FOREIGN_IP_ADDRESS = "netstatForeignIpAddress";
	/** 外部ポート */
	public static final String NETSTAT_FOREIGN_PORT = "netstatForeignPort";
	/** プロセス名 */
	public static final String NETSTAT_PROCESS_NAME = "netstatProcessName";
	/** PID */
	public static final String NETSTAT_PID = "netstatPid";
	/** 状態 */
	public static final String NETSTAT_STATUS = "netstatStatus";

	/** -------------------- */
	/** ----- プロセス ----- */
	/** -------------------- */

	/** ----- プロセス情報 ----- */
	/** プロセス名 */
	public static final String PROCESS_NAME = "processName";
	/** PID */
	public static final String PROCESS_PID = "processPid";
	/** 引数付フルパス */
	public static final String PROCESS_PATH = "processPath";
	/** 実行ユーザ */
	public static final String PROCESS_EXEC_USER = "processExecUser";
	/** 起動日時 */
	public static final String PROCESS_STARTUP_DATE_TIME = "processStartupDateTime";


	/** -------------------- */
	/** ----- パッケージ ----- */
	/** -------------------- */

	/** ----- パッケージ情報 ----- */
	/** パッケージID */
	public static final String PACKAGE_ID = "packageId";
	/** パッケージ名 */
	public static final String PACKAGE_NAME = "packageName";
	/** バージョン */
	public static final String PACKAGE_VERSION = "packageVersion";
	/** リリース番号 */
	public static final String PACKAGE_RELEASE = "packageRelease";
	/** インストール日時 */
	public static final String PACKAGE_INSTALL_DATE = "packageInstallDate";
	/** ベンダ */
	public static final String PACKAGE_VENDOR = "packageVendor";
	/** アーキテクチャ */
	public static final String PACKAGE_ARCHITECTURE = "packageArchitecture";


	/** -------------------- */
	/** ----- 個別導入製品 ----- */
	/** -------------------- */

	/** ----- 個別導入製品情報 ----- */
	/** 名前 */
	public static final String PRODUCT_NAME = "productName";
	/** バージョン */
	public static final String PRODUCT_VERSION = "productVersion";
	/** インストールパス */
	public static final String PRODUCT_PATH = "productPath";


	/** -------------------- */
	/** ----- ライセンス ----- */
	/** -------------------- */

	/** ----- ライセンス情報 ----- */
	/** 製品名 */
	public static final String LICENSE_PRODUCT_NAME = "productName";
	/** ベンダ */
	public static final String LICENSE_VENDOR = "licenseVendor";
	/** ベンダ連絡先 */
	public static final String LICENSE_VENDOR_CONTACT = "licenseVendorContact";
	/** シリアルナンバー */
	public static final String LICENSE_SERIAL_NUMBER = "licenseSerialNumber";
	/** 数量 */
	public static final String LICENSE_COUNT = "licenseCount";
	/** 有効期限 */
	public static final String LICENSE_EXPIRATION_DATE = "licenseExpirationDate";


	
	/** -------------------- */
	/** ----- ノード変数 ----- */
	/** -------------------- */

	/** ----- ノード変数 ----- */
	/** 変数名 */
	public static final String NODE_VARIABLE_NAME = "nodeVariableName";
	/** 値 */
	public static final String NODE_VARIABLE_VALUE = "nodeVariableValue";


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
	/** クラウドログ監視優先度 */
	public static final String CLOUDLOGPRIORITY= "cloudLogPriorty";
	
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
	/** Hinemosエージェント */
	public static final String AGENT = "agent";

	/** ----- 構成情報 ----- */
	/** 構成情報 */
	public static final String NODE_CONFIG_INFORMATION = "nodeConfigInformation";
	/** OS */
	public static final String OS = "os";

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

	/** ----- ネットワーク接続----- */
	public static final String NETSTAT = "netstat";
	public static final String NETSTAT_LIST = "netstatList";

	/** ----- プロセス----- */
	public static final String PROCESS = "process";
	public static final String PROCESS_LIST = "processList";

	/** ----- パッケージ----- */
	public static final String PACKAGE = "package";
	public static final String PACKAGE_LIST = "packageList";

	/** ----- 個別導入製品----- */
	public static final String PRODUCT = "product";
	public static final String PRODUCT_LIST = "productList";

	/** ----- ライセンス----- */
	public static final String LICENSE = "license";
	public static final String LICENSE_LIST = "licenseList";

	/** ----- ノード変数----- */
	/** ノード変数 */
	public static final String NODE_VARIABLE = "nodeVariable";
	/** ノード変数 */
	public static final String GENERAL_NODE_VARIABLE = "generalNodeVariable";

	/** -----ユーザ任意情報----- */
	/** ユーザ任意情報 */
	public static final String CUSTOM = "custom";
	public static final String CUSTOM_LIST = "customList";

	/** -----RPAツール情報----- */
	public static final String RPA_TOOL ="rpaTool";

	/** -----RPA管理ツール情報----- */
	public static final String RPA_MANAGEMENT_TOOL ="rpaManagementTool";
	
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

	/** ----- クラウド・仮想化----- */
	public static final String CLOUD_MANAGEMENT = "cloudManagement";

	/** 保守 */
	public static final String MAINTENANCE = "maintenance";

}
