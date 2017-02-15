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
 * ジョブユニット及びジョブネットの終了状態チェック用の定数を定義するクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class EndStatusCheckConstant {
	/** 待ち条件に指定されていないジョブの終了状態をチェックする */
	public static final int NO_WAIT_JOB = 0;
	/** 全ジョブの終了状態をチェックする */
	public static final int ALL_JOB = 1;
}
