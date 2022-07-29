/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.bean;

/**
 * 通知契機情報<BR>
 * 
 */
public enum NotifyTriggerType {
	// 監視
	MONITOR,
	// 変更点監視
	MONITOR_CHANGE,
	// 将来予測監視
	MONITOR_PREDICTION,
	// ジョブ-開始
	JOB_START,
	// ジョブ-終了
	JOB_END,
	// ジョブ-正常終了
	JOB_END_NORMAL,
	// ジョブ-警告終了
	JOB_END_WARNING,
	// ジョブ-異常終了
	JOB_END_ABNORMAL,
	// ジョブ-開始遅延
	JOB_START_DELAY,
	// ジョブ-終了遅延
	JOB_END_DELAY,
	// ジョブ-同時実行制御待ち開始
	JOB_QUEUE_START,
	// ジョブ-同時実行制御待ち終了
	JOB_QUEUE_END,
	// ジョブ-多重度超過
	JOB_EXCEEDED_MULTIPLICITY,
	// ジョブ-ファイル出力失敗
	JOB_COMMAND_OUTPUT_FAILED,
	// 環境構築-モジュール実行開始
	INFRA_RUN_START,
	// 環境構築-モジュール実行終了
	INFRA_RUN_END,
	// 環境構築-モジュールチェック開始
	INFRA_CHECK_START,
	// 環境構築-モジュールチェック終了
	INFRA_CHECK_END,
	// 構成情報管理
	NODE_CONFIG_SETTING,
	// レポーティング
	REPORTING,
	// RPA-シナリオ実績作成
	RPA_SCENARIO_CREATE,
	// RPA-シナリオ実績更新
	RPA_SCENARIO_UPDATE,
	// 履歴削除
	MAINTENANCE,
	// INTERNALイベント;
	INTERNAL_EVENT,
	// SDML-監視設定作成
	SDML_CONTROL_CREATE_MONITOR,
	// SDML-監視設定更新
	SDML_CONTROL_UPDATE_MONITOR,
	// SDML-監視設定無効化
	SDML_CONTROL_DISABLE_MONITOR,
	// SDML-監視設定有効化
	SDML_CONTROL_ENABLE_MONITOR,
	// SDML-異常時
	SDML_CONTROL_ABNORMAL,
	// SDML-起動から停止までの時間が短い場合
	SDML_CONTROL_EARLY_STOP;
}