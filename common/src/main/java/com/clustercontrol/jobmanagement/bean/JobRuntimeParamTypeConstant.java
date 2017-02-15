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
 * ジョブ[実行契機]の変数種別定数クラス<BR>
 * 
 * @version 5.1.0
 */
public class JobRuntimeParamTypeConstant {
	/** 入力の場合 */
	public static final int TYPE_INPUT= 0;

	/** 選択（ラジオボタン）の場合 */
	public static final int TYPE_RADIO = 1;

	/** 選択（コンボボックス）の場合 */
	public static final int TYPE_COMBO = 2;

	/** 固定の場合 */
	public static final int TYPE_FIXED = 3;
}