/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * RPAシナリオジョブ（間接実行）停止時の挙動を制御するクラス<BR>
 */
public class RpaStopTypeConstant {
	/** シナリオを終了する */
	public static final int STOP_SCENARIO = 0;
	/** シナリオは終了せず、ジョブのみ終了する */
	public static final int STOP_JOB= 1;
}
