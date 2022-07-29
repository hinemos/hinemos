/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;


/**
 * ジョブユニット及びジョブネットの終了状態チェック用の定数を定義するクラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class EndStatusCheckConstant {
	/** 待ち条件に指定されていないジョブの終了状態をチェックする */
	public static final int NO_WAIT_JOB = 0;
	/** 全ジョブの終了状態をチェックする */
	public static final int ALL_JOB = 1;
}
