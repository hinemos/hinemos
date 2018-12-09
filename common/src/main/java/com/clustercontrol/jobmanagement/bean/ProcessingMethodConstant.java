/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブ実行処理方法の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProcessingMethodConstant {
	/** 全てのノードで実行 */
	public static final int TYPE_ALL_NODE = 0;

	/** 正常終了するまでノードを順次リトライ */
	public static final int TYPE_RETRY = 1;

	private ProcessingMethodConstant() {
		throw new IllegalStateException("ConstClass");
	}
}