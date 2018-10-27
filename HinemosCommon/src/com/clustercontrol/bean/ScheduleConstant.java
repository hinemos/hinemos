/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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