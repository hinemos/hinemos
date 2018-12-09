/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.bean;

/**
 * 抑制間隔の定義を定数として格納するクラス<BR>
 *
 * @version 2.1.0
 * @since 1.0.0
 */
public class RenotifyTypeConstant {
	/** 常に通知する（種別）。 */
	public static final int TYPE_ALWAYS_NOTIFY = 0;

	/** 期間で抑制する（分単位）（種別）。 */
	public static final int TYPE_PERIOD = 1;

	/** 再通知しない（種別）。 */
	public static final int TYPE_NO_NOTIFY = 2;

	private RenotifyTypeConstant() {
		throw new IllegalStateException("ConstClass");
	}
}