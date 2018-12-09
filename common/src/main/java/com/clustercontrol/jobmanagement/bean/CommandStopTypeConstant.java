/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * コマンド終了実行時のエージェントの挙動を制御するクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class CommandStopTypeConstant {
	/** 停止コマンドを実行 */
	public static final int EXECUTE_COMMAND = 0;
	/** プロセスを終了 */
	public static final int DESTROY_PROCESS= 1;

	private CommandStopTypeConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
