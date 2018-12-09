/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブ終了状態の定数クラス<BR>
 * 
 * @version 6.0.0
 * @since 1.0.0
 */
public class JudgmentObjectConstant {
	/** ジョブ終了状態 */
	public static final int TYPE_JOB_END_STATUS = 0;

	/** ジョブ終了値 */
	public static final int TYPE_JOB_END_VALUE = 1;

	/** 時刻 */
	public static final int TYPE_TIME = 2;

	/** セッション開始時の時間（分）  */
	public static final int TYPE_START_MINUTE = 3;

	/** ジョブ変数 */
	public static final int TYPE_JOB_PARAMETER = 4;

	/** セッション横断ジョブ終了状態 */
	public static final int TYPE_CROSS_SESSION_JOB_END_STATUS = 5;

	/** セッション横断ジョブ終了値 */
	public static final int TYPE_CROSS_SESSION_JOB_END_VALUE = 6;

	private JudgmentObjectConstant() {
		throw new IllegalStateException("ConstClass");
	}
}