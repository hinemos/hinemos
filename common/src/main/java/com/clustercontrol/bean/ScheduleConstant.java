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
 * スケジュールの種別定数クラス<BR>
 * 
 * @version 4.1.0
 * @since 1.0.0
 */
public class ScheduleConstant {
	/** 毎日・時・分の場合 */
	public static final int TYPE_DAY = 1;

	/** 週・時・分の場合 */
	public static final int TYPE_WEEK = 2;

	/** p分からq分毎に繰り返し実行の場合 */
	public static final int TYPE_REPEAT = 3;
}