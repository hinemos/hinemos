/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
}
