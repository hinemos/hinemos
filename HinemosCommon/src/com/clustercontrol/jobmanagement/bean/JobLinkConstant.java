/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;


/**
 * ジョブ連携の定数クラス<BR>
 *
 */
public class JobLinkConstant {
	/** ジョブ連携送信設定のデフォルトポート */
	public static final int SEND_SETTING_PORT_DEFAUT = 8080;

	/** ジョブ連携メッセージの拡張情報キー（INTERNALイベント） */
	public static final String EXP_KEY_INTERNAL_ID = "INTERNAL_ID";

	/** ジョブ連携送信ジョブの拡張情報の最大数 */
	public static final int EXP_INFO_MAX_COUNT = 99;

	/** ジョブ連携待機ジョブで過去を対象にしない場合の対象期間 (1分) */
	public static final long RCV_TARGET_TIME_PERIOD = 60 * 1000;

	/** ジョブ連携待機ジョブ 戻り値（常に） */
	public static final int RCV_INITIAL_END_VALUE_ALL = 0;
	/** ジョブ連携待機ジョブ 戻り値（情報） */
	public static final int RCV_INITIAL_END_VALUE_INFO = 0;
	/** ジョブ連携待機ジョブ 戻り値（警告） */
	public static final int RCV_INITIAL_END_VALUE_WARN = 1;
	/** ジョブ連携待機ジョブ 戻り値（危険） */
	public static final int RCV_INITIAL_END_VALUE_CRITICAL = 9;
	/** ジョブ連携待機ジョブ 戻り値（不明） */
	public static final int RCV_INITIAL_END_VALUE_UNKNOWN = -1;
	/** ジョブ連携待機ジョブ 戻り値（タイムアウト） */
	public static final int RCV_INITIAL_END_VALUE_TIMEOUT = -1;
	/** ジョブ連携待機ジョブ メッセージ確認期間 */
	public static final int RCV_INITIAL_PAST_MIN = 1;
	/** ジョブ連携待機ジョブ タイムアウト */
	public static final int RCV_INITIAL_TIMEOUT_MIN = 0;
}