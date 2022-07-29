/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import com.clustercontrol.rest.dto.EnumDto;

/**
 * ジョブ連携引継ぎ情報キー
 * 
 * @version 1.0.0
 *
 */
public enum JobLinkInheritKeyInfo implements EnumDto<String> {

	// 送信元ファシリティID
	SOURCE_FACILITY_ID("SOURCE_FACILITY_ID"),
	// 送信元IPアドレス
	SOURCE_IP_ADDRESS("SOURCE_IP_ADDRESS"),
	// ジョブ連携メッセージID
	JOBLINK_MESSAGE_ID("JOBLINK_MESSAGE_ID"),
	// 監視詳細
	MONITOR_DETAIL_ID("MONITOR_DETAIL_ID"),
	// 重要度
	PRIORITY("PRIORITY"),
	// アプリケーション
	APPLICATION("APPLICATION"),
	// メッセージ
	MESSAGE("MESSAGE"),
	// オリジナルメッセージ
	MESSAGE_ORG("MESSAGE_ORG"),
	// 拡張情報
	EXP_INFO("EXP_INFO");

	private final String code;

	private JobLinkInheritKeyInfo(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
