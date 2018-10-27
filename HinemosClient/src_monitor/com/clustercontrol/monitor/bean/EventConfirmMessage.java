/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

import com.clustercontrol.util.Messages;

/**
 * イベント通知の状態の定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class EventConfirmMessage {
	/** 確認済（文字列）。 */
	public static final String STRING_CONFIRMED = Messages.getString("notify.event.confirmed");

	/** 未確認（文字列）。 */
	public static final String STRING_UNCONFIRMED = Messages.getString("notify.event.unconfirmed");

	/** 破棄（文字列）。 */
	public static final String STRING_DESTRUCTION = Messages.getString("notify.event.destruction");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == EventConfirmConstant.TYPE_CONFIRMED) {
			return STRING_CONFIRMED;
		} else if (type == EventConfirmConstant.TYPE_UNCONFIRMED) {
			return STRING_UNCONFIRMED;
		} else if (type == EventConfirmConstant.TYPE_DESTRUCTION) {
			return STRING_DESTRUCTION;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_CONFIRMED)) {
			return EventConfirmConstant.TYPE_CONFIRMED;
		} else if (string.equals(STRING_UNCONFIRMED)) {
			return EventConfirmConstant.TYPE_UNCONFIRMED;
		} else if (string.equals(STRING_DESTRUCTION)) {
			return EventConfirmConstant.TYPE_DESTRUCTION;
		}
		return -1;
	}

}