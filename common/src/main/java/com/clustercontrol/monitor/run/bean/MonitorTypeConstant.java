/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.bean;

/**
 * 監視種別の定義を定数として格納するクラス<BR>
 * 
 * @version 6.1.0 バイナリ監視追加
 * @since 2.1.0
 */
public class MonitorTypeConstant {
	/** 真偽値（種別）。 */
	public static final int TYPE_TRUTH = 0;

	/** 数値（種別）。 */
	public static final int TYPE_NUMERIC = 1;

	/** 文字列（種別）。 */
	public static final int TYPE_STRING = 2;

	/** トラップ（種別）。 */
	public static final int TYPE_TRAP = 3;

	/** シナリオ（種別）。 */
	public static final int TYPE_SCENARIO = 4;	

	/** バイナリ（種別）。 */
	public static final int TYPE_BINARY = 5;	

	private MonitorTypeConstant() {
		throw new IllegalStateException("ConstClass");
	}
}