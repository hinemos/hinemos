/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ping.bean;

/**
 * ping実行間隔の定義を定数として格納するクラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class PingRunIntervalConstant {

	/** 1秒（種別）。 */
	public static final int TYPE_SEC_01 = 1000;

	/** 2秒（種別）。 */
	public static final int TYPE_SEC_02 = 2000;

	/** 5秒（種別）。 */
	public static final int TYPE_SEC_05 = 5000;

	private PingRunIntervalConstant() {
		throw new IllegalStateException("ConstClass");
	}
}