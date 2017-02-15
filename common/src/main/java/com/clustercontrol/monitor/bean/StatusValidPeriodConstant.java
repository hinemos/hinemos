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

package com.clustercontrol.monitor.bean;

/**
 * ステータス情報の存続期間の定義を定数として格納するクラスです。
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class StatusValidPeriodConstant {

	/** 無期限（種別）。 */
	public static final int TYPE_UNLIMITED = -1;

	/** 10分（種別）。 */
	public static final int TYPE_MIN_10 = 10;

	/** 20分（種別）。 */
	public static final int TYPE_MIN_20 = 20;

	/** 30分（種別）。 */
	public static final int TYPE_MIN_30 = 30;

	/** 1時間（種別）。 */
	public static final int TYPE_HOUR_1 = 60;

	/** 3時間（種別）。 */
	public static final int TYPE_HOUR_3 = 180;

	/** 6時間（種別）。 */
	public static final int TYPE_HOUR_6 = 360;

	/** 12時間（種別）。 */
	public static final int TYPE_HOUR_12 = 720;

	/** 1日（種別）。 */
	public static final int TYPE_DAY_1 = 1440;
}