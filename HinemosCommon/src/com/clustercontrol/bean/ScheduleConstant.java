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
	/** [毎日]・時・分の場合 */
	public static final int TYPE_DAY = 1;

	/** [毎週]週・時・分の場合 */
	public static final int TYPE_WEEK = 2;

	/** [毎時]p分からq分毎に繰り返し実行の場合 */
	public static final int TYPE_REPEAT = 3;

	/** [一定間隔]・時・分から・q分毎に繰り返し実行の場合 */
	public static final int TYPE_INTERVAL = 4;
}