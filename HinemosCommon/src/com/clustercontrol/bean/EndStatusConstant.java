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

package com.clustercontrol.bean;

/**
 * ジョブ終了状態の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class EndStatusConstant {
	/** 正常 */
	public static final int TYPE_NORMAL = 0;

	/** 警告 */
	public static final int TYPE_WARNING = 1;

	/** 異常 */
	public static final int TYPE_ABNORMAL = 2;

	/** 開始 */
	public static final int TYPE_BEGINNING = 3;

	/** すべての終了状態 */
	public static final int TYPE_ANY = 4;

	/** 正常 */
	public static final int INITIAL_VALUE_NORMAL = 0;

	/** 警告 */
	public static final int INITIAL_VALUE_WARNING = 1;

	/** 異常 */
	public static final int INITIAL_VALUE_ABNORMAL = -1;
}