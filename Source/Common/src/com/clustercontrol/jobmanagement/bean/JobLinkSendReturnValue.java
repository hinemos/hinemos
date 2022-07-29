/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * ジョブ連携送信ジョブのノード戻り値の定数クラス<BR>
 * 
 */
public enum JobLinkSendReturnValue {

	/** 送信成功 */
	SUCCESS(0),
	/** 送信失敗 */
	FAILURE(9);

	private final int value;

	private JobLinkSendReturnValue(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}