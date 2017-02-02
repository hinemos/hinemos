/*

Copyright (C) 2016 NTT DATA Corporation

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
 * 監視ジョブの初期値を定義するクラス<BR>
 *
 * @version 6.0.0
 */
public class MonitorJobConstant {
	/** 戻り値（情報） */
	public static final int INITIAL_END_VALUE_INFO = 0;
	/** 戻り値（警告） */
	public static final int INITIAL_END_VALUE_WARN = 1;
	/** 戻り値（危険） */
	public static final int INITIAL_END_VALUE_CRITICAL = 9;
	/** 戻り値（不明） */
	public static final int INITIAL_END_VALUE_UNKNOWN = -1;
	/** 待ち間隔 */
	public static final int INITIAL_WAIT_INTERVAL_MINUTE = 1;
}
