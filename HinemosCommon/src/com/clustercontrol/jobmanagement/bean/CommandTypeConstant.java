/*

Copyright (C) 2006 NTT DATA Corporation

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
