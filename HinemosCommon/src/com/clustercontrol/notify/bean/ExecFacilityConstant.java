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

package com.clustercontrol.notify.bean;

/**
 * 対象ファシリティの定義を定数として格納するクラス<BR>
 * 
 * @version 3.0.0
 * @since 3.0.0
 */
public class ExecFacilityConstant {
	/** イベント発生ノード（種別）。 */
	public static final int TYPE_GENERATION = 0;

	/** 固定スコープ（種別）。 */
	public static final int TYPE_FIX = 1;
}