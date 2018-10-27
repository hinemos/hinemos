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
 * イベント情報の確認状態の定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConfirmMessage {
	/** 確認（文字列）。 */
	public static final String STRING_CONFIRMED = Messages.getString("monitor.confirmed");

	/** 未確認（文字列）。 */
	public static final String STRING_UNCONFIRMED = Messages.getString("monitor.unconfirmed");


	/** 確認（真偽）。 */
	public static final boolean BOOLEAN_CONFIRMED = true;

	/** 未確認（真偽）。 */
	public static final boolean BOOLEAN_UNCONFIRMED = false;

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == ConfirmConstant.TYPE_CONFIRMED) {
			return STRING_CONFIRMED;
		} else if (type == ConfirmConstant.TYPE_UNCONFIRMED) {
			return STRING_UNCONFIRMED;
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
			return ConfirmConstant.TYPE_CONFIRMED;
		} else if (string.equals(STRING_UNCONFIRMED)) {
			return ConfirmConstant.TYPE_UNCONFIRMED;
		}
		return ConfirmConstant.TYPE_ALL;
	}

	/**
	 * 種別から真偽に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 真偽
	 */
	public static boolean typeToBoolean(int type) {
		if (type == ConfirmConstant.TYPE_CONFIRMED) {
			return BOOLEAN_CONFIRMED;
		} else if (type == ConfirmConstant.TYPE_UNCONFIRMED) {
			return BOOLEAN_UNCONFIRMED;
		}
		return false;
	}

	/**
	 * 真偽から種別に変換します。<BR>
	 * @param bool 真偽
	 * @return 種別
	 */
	public static int booleanToType(boolean bool) {
		return bool == BOOLEAN_CONFIRMED ? ConfirmConstant.TYPE_CONFIRMED: ConfirmConstant.TYPE_UNCONFIRMED;
	}
}