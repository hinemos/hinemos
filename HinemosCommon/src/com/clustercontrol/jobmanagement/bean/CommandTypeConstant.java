/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;


/**
 * ジョブ実行時にエージェントにて実行するコマンドのタイプの定数を定義するクラス<BR>
 *
 * @version 2.1.0
 * @since 1.0.0
 */
public class CommandTypeConstant {
	/** 通常コマンド */
	public static final int NORMAL = 0;
	/** 停止コマンド */
	public static final int STOP = 1;
}
