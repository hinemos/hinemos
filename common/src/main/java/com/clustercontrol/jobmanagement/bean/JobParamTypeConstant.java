/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブパラメータの定数クラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class JobParamTypeConstant {
	/** システムパラメータ（ジョブ） */
	public static final int TYPE_SYSTEM_JOB = 0;

	/** システムパラメータ（ノード） */
	public static final int TYPE_SYSTEM_NODE = 1;

	/** ユーザパラメータ */
	public static final int TYPE_USER = 2;

	/** ランタイムパラメータ */
	public static final int TYPE_RUNTIME = 3;

	private JobParamTypeConstant() {
		throw new IllegalStateException("ConstClass");
	}
}