/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.util;

/**
 * RPA管理機能向けの定数を格納するクラス
 *
 */
public class RpaConstants {
	/**
	 *  RPAログ解析処理を呼び出すQuartzのグループ名。
	 */
	public static final String groupName = "RpaAnalyzer";

	/**
	 *  Quartzから呼び出すRPAログ解析処理を行う Session Bean のメソッド名。
	 */
	public static final String methodName = "start";

	/** 
	 * Quartzスケジュール名の接頭詞<BR>
	 */
	public static final String scheduleIdHeader = "Parse-";
	
	
	/**
	 * RPAスコープ
	 */
	public static final String RPA = "_RPA";
	
	/**
	 * 管理製品なし(WinActor)
	 */
	public static final String RPA_NO_MGR_WINACTOR = "_RPA_NO_MGR_WINACTOR"; 

	/**
	 * 管理製品なし(UiPath)
	 */
	public static final String RPA_NO_MGR_UIPATH = "_RPA_NO_MGR_UIPATH"; 

	// プラットフォームマスタ内におけるRPA管理ツールマスタのorderNo
	public static final int rpaManagementToolPlatformOrderNo = 120;
	
	// RPAサブプラットフォームマスタのtype
	public static final String rpaSubPlatformType = "RPA";
	
	// 自動検知で登録するノードの説明
	public static final String autoRegistNodeDescription = "Hinemos Auto Registered";
	
	// 組み込みで登録するRPAシナリオ実績の履歴削除設定ID
	public static final String rpaScenarioOperationResultMaintenanceId = "MT-RPA-DEFAULT";

}
