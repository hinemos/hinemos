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
 * ジョブ終了状態の定数クラス<BR>
 * 
 * @version 6.0.0
 * @since 1.0.0
 */
public class JudgmentObjectConstant {
	/** ジョブ終了状態 */
	public static final int TYPE_JOB_END_STATUS = 0;

	/** ジョブ終了値 */
	public static final int TYPE_JOB_END_VALUE = 1;

	/** 時刻 */
	public static final int TYPE_TIME = 2;

	/** セッション開始時の時間（分）  */
	public static final int TYPE_START_MINUTE = 3;

	/** ジョブ変数 */
	public static final int TYPE_JOB_PARAMETER = 4;
}