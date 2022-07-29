/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

/**
 * イベント情報の履歴種別の定義を定数として格納するクラス<BR>
 * 
 */
public class EventLogHistoryTypeConstant {
	
	/** 未確認に変更。 */
	public static final int TYPE_CHANGE_UNCONFIRMED  = 11;

	/** 確認中に変更。 */
	public static final int TYPE_CHANGE_CONFIRMING = 12;
	
	/** 確認済に変更。 */
	public static final int TYPE_CHANGE_CONFIRMED = 13;

	/** イベントの値変更。 */
	public static final int TYPE_CHANGE_VALUE  = 21;

	/** カスタムイベントコマンド開始。 */
	public static final int TYPE_COMMAND_START = 31;
	
	/** カスタムイベントコマンド詳細。 */
	public static final int TYPE_COMMAND_DETAIL = 32;

	/** カスタムイベントコマンド終了。 */
	public static final int TYPE_COMMAND_END = 33;
	
	/** カスタムイベントコマンドスキップ。(前のイベントのタイムアウトによりキャンセル) */
	public static final int TYPE_COMMAND_SKIP = 34;
	
}