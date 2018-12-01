/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.bean;

/**
 * 環境構築のアクセス情報の切り替えを定義するクラス<BR>
 *
 * @version 6.1.0
 */
public class InfraNodeInputConstant {

	// アクセス情報切り替え
	/** ノード変数を使用 */
	public static final int TYPE_NODE_PARAM = 0;
	/** 環境構築変数を使用 */
	public static final int TYPE_INFRA_PARAM = 1;
	/** ダイアログにより入力 */
	public static final int TYPE_DIALOG = 2;
	
	/** デフォルト値 */
	public static final int TYPE_DEFAULT = TYPE_NODE_PARAM;

	private InfraNodeInputConstant() {
		throw new IllegalStateException("ConstClass");
	}
}