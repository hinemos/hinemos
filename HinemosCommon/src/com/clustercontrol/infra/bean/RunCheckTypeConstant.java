/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.bean;

/**
 * 環境構築機能のチェックと実行のタイプを定数として格納するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class RunCheckTypeConstant {
	/** 実行 */
	public static final int TYPE_RUN = 1;
	/** チェック */
	public static final int TYPE_CHECK = 2;
	/** プレチェック */
	public static final int TYPE_PRECHECK = 3;
}
