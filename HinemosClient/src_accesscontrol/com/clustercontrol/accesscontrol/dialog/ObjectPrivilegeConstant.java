/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.dialog;

import java.util.Arrays;
import java.util.List;

import com.clustercontrol.bean.HinemosModuleConstant;

/**
 * 
 * オブジェクト権限に関する定数を保持するクラス
 *
 */
public class ObjectPrivilegeConstant {

	/** 実行権限を持たないオブジェクトタイプ（機能）のリスト */
	public static final List<String> OBJECT_TYPE_LIST_OF_NOT_HAVING_EXECUTE_PRIVILEGE = Arrays.asList(
			// リポジトリ：スコープ
			HinemosModuleConstant.PLATFORM_REPOSITORY,
			// 監視設定：通知
			HinemosModuleConstant.PLATFORM_NOTIFY,
			// 監視設定：メールテンプレート
			HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE,
			// 監視設定：ログフォーマット
			HinemosModuleConstant.HUB_LOGFORMAT,
			// 監視設定：コマンド通知テンプレート
			HinemosModuleConstant.PLATFORM_COMMAND_TEMPLATE,
			// 監視設定：RESTアクセス設定
			HinemosModuleConstant.PLATFORM_REST_ACCESS,
			// 監視設定：一覧
			HinemosModuleConstant.MONITOR,
			// ジョブ設定：実行契機
			HinemosModuleConstant.JOB_KICK,
			// ジョブ設定：同時実行制御
			HinemosModuleConstant.JOB_QUEUE,
			// 環境構築：ファイル
			HinemosModuleConstant.INFRA_FILE,
			// カレンダ：カレンダ
			HinemosModuleConstant.PLATFORM_CALENDAR,
			// カレンダパターン
			HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN,
			// メンテナンス：履歴情報削除
			HinemosModuleConstant.SYSYTEM_MAINTENANCE,
			// 共通：フィルタ設定
			HinemosModuleConstant.FILTER_SETTING,
			// 収集蓄積：転送
			HinemosModuleConstant.HUB_TRANSFER,
			// SDML:SDML制御設定
			HinemosModuleConstant.SDML_CONTROL,
			// RPA:RPAシナリオ
			HinemosModuleConstant.RPA_SCENARIO,
			// RPA:RPAシナリオ実績作成設定
			HinemosModuleConstant.RPA_SCENARIO_CREATE
			
			);
}
