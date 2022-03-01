/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.bean;

// TODO SystemPrivilegeFunction に移行したのでクライアント側の影響を確認したら削除する

/**
 * Hinemosのシステム権限の機能名を定数として格納するクラス<BR>
 * 
 * @version 6.0.0
 * @since 2.0.0
 */
public final class FunctionConstant {

	/** Hinemosエージェント(内部用) */
	public static final String HINEMOS_AGENT = "HinemosAgent";
	/** Hinemos HA(内部用) */
	public static final String HINEMOS_HA = "HinemosHA";
	/** Hinemos CLI(内部用) */
	public static final String HINEMOS_CLI = "HinemosCLI";

	/** リポジトリ */
	public static final String REPOSITORY = "Repository";
	/** ユーザ管理 */
	public static final String ACCESSCONTROL = "AccessControl";
	/** ジョブ管理 */
	public static final String JOBMANAGEMENT = "JobManagement";
	/** 収集管理 */
	public static final String COLLECT = "Collect";
	/** 監視結果 */
	public static final String MONITOR_RESULT = "MonitorResult";
	/** 監視設定 */
	public static final String MONITOR_SETTING = "MonitorSetting";
	/** カレンダ */
	public static final String CALENDAR = "Calendar";
	/** 通知 */
	public static final String NOTIFY = "Notify";
	/** 環境構築機能 */
	public static final String INFRA = "Infra";
	/** メンテナンス(履歴情報削除, 共通設定) */
	public static final String MAINTENANCE = "Maintenance";
	/** クラウド・仮想化管理 */
	public static final String CLOUDMANAGEMENT = "CloudManagement";
	/** レポーティング */
	public static final String REPORTING = "Reporting";
	/** 収集蓄積 */
	public static final String HUB = "Hub";
	/** フィルタ設定 */
	public static final String FILTER_SETTING = "FilterSetting";
	/** SDML設定 */
	public static final String SDML_SETTING = "SdmlSetting";
	/** RPA管理 */
	public static final String RPA = "Rpa";
}