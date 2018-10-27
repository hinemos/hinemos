/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.bean;

/**
 * コマンド監視の定数クラス<BR />
 * 
 * @since 4.0
 */
public final class CustomConstant {

	/**
	 *  コマンドの実行種別(INDIVIDUAL : スコープ内の全ノード, SELECTED : 選択した特定のノード)
	 */
	public static enum CommandExecType { INDIVIDUAL, SELECTED };

	// 上記に対応するDBカラム値
	public static final int _execIndividual = 1;
	public static final int _execSelected = 2;

}
