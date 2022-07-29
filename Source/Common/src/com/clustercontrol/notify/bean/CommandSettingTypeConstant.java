/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.bean;

/**
 * コマンド通知のコマンド設定種別を定義するクラス
 * 
 * 定数値 0 は利用不可（設定エクスポートインポートのXML変換の都合上）
 * 
 */
public class CommandSettingTypeConstant {
	/** コマンド入力 */
	public static final int DIRECT_COMMAND = 1;

	/** コマンドテンプレート選択 */
	public static final int CHOICE_TEMPLATE = 2;
}