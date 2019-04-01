/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.repository;

public class NodeConfigConstant {

	// NodeConfigCollector.java全体向け定義.
	/** NodeConfigCollectorを使う機能定義 **/
	public static enum Function {
		/** 定期的な構成情報収集 **/
		REGULAR_COLLECT,
		/** ノード自動登録 **/
		NODE_REGISTER,
		/** 構成情報収集の即時実行 **/
		RUN_COLLECT
	}

	// ファイル拡張子.
	/** Linux向けスクリプト拡張子 **/
	protected static final String SCRIPT_EXTENSION_LINUX = ".sh";
	/** Windows向けスクリプト拡張子 **/
	protected static final String SCRIPT_EXTENSION_WIN = ".ps1";
	/** TSV拡張子 **/
	protected static final String TSV_EXTENSION = ".tsv";

	// スクリプト/TSVファイル実行結果ステータス.
	protected static enum Result {
		SUCCESS,
		PARTIALLY_SUCCESS
	}

	// ***************************************************
	// *
	// * 以下、各スクリプト・TSVファイル向けの定義.
	// * 変更する場合はスクリプトの実装と平仄を取ること.
	// *
	// ***************************************************

	// TSVファイルの各行のデータ種類を表すヘッダ
	/** TSV行ヘッダ_プロセスデータ **/
	protected static final String LINE_HEADER_PROCESS = "process";
	/** TSV行ヘッダ_パッケージデータ **/
	protected static final String LINE_HEADER_PACKAGE = "package";
	/** TSV行ヘッダ_NICデータ **/
	protected static final String LINE_HEADER_NIC = "nic";
	/** TSV行ヘッダ_CPUデータ **/
	protected static final String LINE_HEADER_CPU = "cpu";
	/** TSV行ヘッダ_OSデータ **/
	protected static final String LINE_HEADER_OS = "os";
	/** TSV行ヘッダ_ホストデータ **/
	protected static final String LINE_HEADER_HOST = "host";
	/** TSV行ヘッダ_メモリデータ **/
	protected static final String LINE_HEADER_MEMORY = "mem";
	/** TSV行ヘッダ_ディスクデータ **/
	protected static final String LINE_HEADER_DISK = "disk";
	/** TSV行ヘッダ_ファイルシステムデータ **/
	protected static final String LINE_HEADER_FSYSTEM = "filesystem";
	/** TSV行ヘッダ_ネットワーク接続情報データ **/
	protected static final String LINE_HEADER_NETSTAT = "netstat";

	// スクリプト共通オプション.
	/** 出力ファイル名指定オプション **/
	protected static final String OPTION_FILENAME = "-f";

	// スクリプト共通終了ステータス(1～255で定義すること).
	/** 処理正常終了 **/
	protected static final int EXCD_SUCCESS = 0;

	/** Windows PowerShell System.AccessViolationException発生 **/
	protected static final int EXCD_SYSTEM_WIN_AVEXCEP = 255;

	/** Hinemos定義_処理続行(取得不可の対象ステータスが加算される) **/
	protected static final int EXCD_HINEMOS_CONTINUE = 10;
	/** 取得不可ステータス_OS情報 **/
	protected static final int FAILED_OS = 1;
	/** 取得不可ステータス_ホスト **/
	protected static final int FAILED_HOST = 2;
	/** 取得不可ステータス_メモリ **/
	protected static final int FAILED_MEMORY = 4;
	/** 処理続行不可ステータス_最大値 **/
	protected static final int EXCD_HINEMOS_CONTINUE_MAX = EXCD_HINEMOS_CONTINUE //
			+ FAILED_OS + FAILED_HOST + FAILED_MEMORY;

	// --プロセススクリプト.
	/** プロセススクリプト名 **/
	protected static final String SCRIPT_NAME_PROCESS = "cmdb_process";
	// プロセス TSV順序
	/** プロセス名 **/
	protected static final int PROCESS_NAME = 1;
	/** 引数付フルパス **/
	protected static final int PROCESS_PATH = 2;
	/** 実行ユーザ **/
	protected static final int PROCESS_USER = 3;
	/** PID **/
	protected static final int PROCESS_ID = 4;
	/** 起動日時 **/
	protected static final int PROCESS_UPTIME = 5;

	// --パッケージスクリプト.
	/** パッケージスクリプト名 **/
	protected static final String SCRIPT_NAME_PACKAGE = "cmdb_package";
	// パッケージ TSV順序
	/** パッケージ名 **/
	protected static final int PACKAGE_NAME = 1;
	/** リリース **/
	protected static final int PACKAGE_RELEASE = 2;
	/** バージョン **/
	protected static final int PACKAGE_VERSION = 3;
	/** インストール日時 **/
	protected static final int PACKAGE_INSTALL_DATE = 4;
	/** ベンダ **/
	protected static final int PACKAGE_VENDOR = 5;
	/** アーキテクチャ **/
	protected static final int PACKAGE_ARCHITECTURE = 6;
	/** 一意識別情報(OS別) **/
	protected static final int PACKAGE_ID = 7;

	// --HW-NICスクリプト.
	/** HWスクリプト名 **/
	protected static final String SCRIPT_NAME_HW_NIC = "cmdb_hw_nic";

	// NIC TSV順序
	/** 表示名 **/
	protected static final int NIC_NAME = 1;
	/** デバイス名 **/
	protected static final int NIC_DEVICE_NAME = 2;
	/** デバイスINDEX **/
	protected static final int NIC_DEVICE_INDEX = 3;
	/** デバイス種別 **/
	protected static final int NIC_DEVICE_TYPE = 4;
	/** デバイスサイズ **/
	protected static final int NIC_DEVICE_SIZE = 5;
	/** デバイスサイズ単位 **/
	protected static final int NIC_DEVICE_SIZE_UNIT = 6;
	/** 説明 **/
	protected static final int NIC_DESCRIPTION = 7;
	/** IPアドレス(v4優先、v6のみの場合はv6) **/
	protected static final int NIC_IP_ADDRESS = 8;
	/** MACアドレス **/
	protected static final int NIC_MAC_ADDRESS = 9;

	// NIC TSVデフォルト値.
	/** デバイス種別 **/
	protected static final String DEFAULT_NIC_TYPE = "nic";

	// --HW-CPUスクリプト.
	/** HWスクリプト名 **/
	protected static final String SCRIPT_NAME_HW_CPU = "cmdb_hw_cpu";

	// CPU TSV順序
	/** 表示名 **/
	protected static final int CPU_NAME = 1;
	/** デバイス名 **/
	protected static final int CPU_DEVICE_NAME = 2;
	/** デバイスINDEX **/
	protected static final int CPU_DEVICE_INDEX = 3;
	/** デバイス種別 **/
	protected static final int CPU_DEVICE_TYPE = 4;
	/** デバイスサイズ **/
	protected static final int CPU_DEVICE_SIZE = 5;
	/** デバイスサイズ単位 **/
	protected static final int CPU_DEVICE_SIZE_UNIT = 6;
	/** 説明 **/
	protected static final int CPU_DESCRIPTION = 7;
	/** コア数 **/
	protected static final int CPU_CORE_COUNT = 8;
	/** スレッド数 **/
	protected static final int CPU_THREAD_COUNT = 9;
	/** クロック数 **/
	protected static final int CPU_CLOCK_COUNT = 10;

	// --HW-Diskスクリプト.
	/** HW-Diskスクリプト名 **/
	protected static final String SCRIPT_NAME_HW_DISK = "cmdb_hw_disk";

	// Disk TSV順序
	/** 表示名 **/
	protected static final int DISK_NAME = 1;
	/** デバイス名 **/
	protected static final int DISK_DEVICE_NAME = 2;
	/** デバイスINDEX **/
	protected static final int DISK_DEVICE_INDEX = 3;
	/** デバイス種別 **/
	protected static final int DISK_DEVICE_TYPE = 4;
	/** デバイスサイズ **/
	protected static final int DISK_DEVICE_SIZE = 5;
	/** デバイスサイズ単位 **/
	protected static final int DISK_DEVICE_SIZE_UNIT = 6;
	/** 説明 **/
	protected static final int DISK_DESCRIPTION = 7;

	// --HW-ファイルシステムスクリプト.
	/** HW-ファイルシステムスクリプト名 **/
	protected static final String SCRIPT_NAME_HW_FSYSTEM = "cmdb_hw_filesystem";

	// ファイルシステム TSV順序
	/** 表示名 **/
	protected static final int FSYSTEM_NAME = 1;
	/** デバイス名 **/
	protected static final int FSYSTEM_DEVICE_NAME = 2;
	/** デバイスINDEX **/
	protected static final int FSYSTEM_DEVICE_INDEX = 3;
	/** デバイス種別 **/
	protected static final int FSYSTEM_DEVICE_TYPE = 4;
	/** デバイスサイズ **/
	protected static final int FSYSTEM_DEVICE_SIZE = 5;
	/** デバイスサイズ単位 **/
	protected static final int FSYSTEM_DEVICE_SIZE_UNIT = 6;
	/** 説明 **/
	protected static final int FSYSTEM_DESCRIPTION = 7;
	/** ファイルシステム種別 **/
	protected static final int FSYSTEM_TYPE = 8;

	// --ネットワーク接続情報スクリプト.
	/** HW-ファイルシステムスクリプト名 **/
	protected static final String SCRIPT_NAME_NETSTAT = "cmdb_netstat";

	// ネットワーク接続情報 TSV順序
	/** プロトコル **/
	protected static final int NETSTAT_PROTOCOL = 1;
	/** ローカルIPアドレス **/
	protected static final int NETSTAT_LOCAL_IP_ADDRESS = 2;
	/** ローカルポート **/
	protected static final int NETSTAT_LOCAL_PORT = 3;
	/** 外部IPアドレス **/
	protected static final int NETSTAT_FOREIGN_IP_ADDRESS = 4;
	/** 外部ポート **/
	protected static final int NETSTAT_FOREIGN_PORT = 5;
	/** プロセス名 **/
	protected static final int NETSTAT_PROCESS_NAME = 6;
	/** プロセスID **/
	protected static final int NETSTAT_PROCESS_ID = 7;
	/** 状態 **/
	protected static final int NETSTAT_STATUS = 8;

	// --その他スクリプト(単行データの集合スクリプト).
	/** その他スクリプト名 **/
	protected static final String SCRIPT_NAME_OTHER = "cmdb_other";

	// スクリプト実行時に実行対象を指定するためのオプション.
	/** OS情報取得オプション **/
	protected static final String EXEC_OPTION_OS = "-o";
	/** ホスト名取得オプション **/
	protected static final String EXEC_OPTION_HOST = "-h";
	/** メモリ取得オプション **/
	protected static final String EXEC_OPTION_MEMORY = "-m";

	// OS TSV順序
	/** OS名 **/
	protected static final int OS_NAME = 1;
	/** OSリリース **/
	protected static final int OS_RELEASE = 2;
	/** OSバージョン **/
	protected static final int OS_VERSION = 3;
	/** 起動日時 **/
	protected static final int STARTUP_DATE_TIME = 4;

	// ホスト名 TSV順序
	/** ホスト名 **/
	protected static final int HOST_NAME = 1;

	// メモリ TSV順序
	/** 表示名 **/
	protected static final int MEMORY_NAME = 1;
	/** デバイス名 **/
	protected static final int MEMORY_DEVICE_NAME = 2;
	/** デバイスINDEX **/
	protected static final int MEMORY_DEVICE_INDEX = 3;
	/** デバイス種別 **/
	protected static final int MEMORY_DEVICE_TYPE = 4;
	/** デバイスサイズ **/
	protected static final int MEMORY_DEVICE_SIZE = 5;
	/** デバイスサイズ単位 **/
	protected static final int MEMORY_DEVICE_SIZE_UNIT = 6;
}
