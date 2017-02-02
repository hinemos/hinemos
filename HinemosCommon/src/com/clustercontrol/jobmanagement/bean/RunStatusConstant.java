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
 * ジョブエージェントのコマンド実行状態の定数を定義するクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class RunStatusConstant {
	/** 開始 */
	public static final int START = 0;
	/** 終了 */
	public static final int END = 1;
	/** 失敗 */
	public static final int ERROR = 2;
}
