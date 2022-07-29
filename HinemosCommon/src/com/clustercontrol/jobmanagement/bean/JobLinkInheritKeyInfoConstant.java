/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import com.clustercontrol.util.Messages;

/**
 * ジョブ連携引継ぎ情報キー
 * 
 */
public class JobLinkInheritKeyInfoConstant {

	// 送信元ファシリティID
	public static final String STRING_SOURCE_FACILITY_ID = Messages.getString("source.facility.id");
	// 送信元IPアドレス
	public static final String STRING_SOURCE_IP_ADDRESS = Messages.getString("source.ip.address");
	// ジョブ連携メッセージID
	public static final String STRING_JOBLINK_MESSAGE_ID = Messages.getString("joblink.message.id");
	// 監視詳細
	public static final String STRING_MONITOR_DETAIL_ID = Messages.getString("monitor.detail.id");
	// 重要度
	public static final String STRING_PRIORITY = Messages.getString("priority");
	// アプリケーション
	public static final String STRING_APPLICATION = Messages.getString("application");
	// メッセージ
	public static final String STRING_MESSAGE = Messages.getString("message");
	// オリジナルメッセージ
	public static final String STRING_MESSAGE_ORG = Messages.getString("message.org");
	// 拡張情報
	public static final String STRING_EXP_INFO = Messages.getString("extended.info");

	/**
	 * 種別からメッセージに変換します。<BR>
	 * 
	 * @param type
	 * @return メッセージ
	 */
	public static String typeToMsg(JobLinkInheritKeyInfo type) {
		if (type == JobLinkInheritKeyInfo.SOURCE_FACILITY_ID) {
			return STRING_SOURCE_FACILITY_ID;
		} else if (type == JobLinkInheritKeyInfo.SOURCE_IP_ADDRESS) {
			return STRING_SOURCE_IP_ADDRESS;
		} else if (type == JobLinkInheritKeyInfo.JOBLINK_MESSAGE_ID) {
			return STRING_JOBLINK_MESSAGE_ID;
		} else if (type == JobLinkInheritKeyInfo.MONITOR_DETAIL_ID) {
			return STRING_MONITOR_DETAIL_ID;
		} else if (type == JobLinkInheritKeyInfo.PRIORITY) {
			return STRING_PRIORITY;
		} else if (type == JobLinkInheritKeyInfo.APPLICATION) {
			return STRING_APPLICATION;
		} else if (type == JobLinkInheritKeyInfo.MESSAGE) {
			return STRING_MESSAGE;
		} else if (type == JobLinkInheritKeyInfo.MESSAGE_ORG) {
			return STRING_MESSAGE_ORG;
		} else if (type == JobLinkInheritKeyInfo.EXP_INFO) {
			return STRING_EXP_INFO;
		}
		return "";
	}

	/**
	 * メッセージから種別に変換します。<BR>
	 * 
	 * @param msg
	 * @return 種別
	 */
	public static JobLinkInheritKeyInfo msgToType(String msg) {
		if (msg.equals(STRING_SOURCE_FACILITY_ID)) {
			return JobLinkInheritKeyInfo.SOURCE_FACILITY_ID;
		} else if (msg.equals(STRING_SOURCE_IP_ADDRESS)) {
			return JobLinkInheritKeyInfo.SOURCE_IP_ADDRESS;
		} else if (msg.equals(STRING_JOBLINK_MESSAGE_ID)) {
			return JobLinkInheritKeyInfo.JOBLINK_MESSAGE_ID;
		} else if (msg.equals(STRING_MONITOR_DETAIL_ID)) {
			return JobLinkInheritKeyInfo.MONITOR_DETAIL_ID;
		} else if (msg.equals(STRING_PRIORITY)) {
			return JobLinkInheritKeyInfo.PRIORITY;
		} else if (msg.equals(STRING_APPLICATION)) {
			return JobLinkInheritKeyInfo.APPLICATION;
		} else if (msg.equals(STRING_MESSAGE)) {
			return JobLinkInheritKeyInfo.MESSAGE;
		} else if (msg.equals(STRING_MESSAGE_ORG)) {
			return JobLinkInheritKeyInfo.MESSAGE_ORG;
		} else if (msg.equals(STRING_EXP_INFO)) {
			return JobLinkInheritKeyInfo.EXP_INFO;
		}
		return null;
	}
}
