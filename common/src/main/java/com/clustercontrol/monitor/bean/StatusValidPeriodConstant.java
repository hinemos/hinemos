/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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

	private StatusValidPeriodConstant() {
		throw new IllegalStateException("ConstClass");
	}
}