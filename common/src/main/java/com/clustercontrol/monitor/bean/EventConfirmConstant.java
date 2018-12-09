/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

/**
 * イベント通知の状態の定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class EventConfirmConstant {
	/** 確認済（種別）。 */
	public static final int TYPE_CONFIRMED = ConfirmConstant.TYPE_CONFIRMED;

	/** 未確認（種別）。 */
	public static final int TYPE_UNCONFIRMED = ConfirmConstant.TYPE_UNCONFIRMED;

	/** 破棄（種別）。 */
	public static final int TYPE_DESTRUCTION = 2;

	private EventConfirmConstant() {
		throw new IllegalStateException("ConstClass");
	}
}