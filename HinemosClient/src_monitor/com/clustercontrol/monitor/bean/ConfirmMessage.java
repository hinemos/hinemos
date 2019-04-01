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

	/** 確認中（文字列）。 */
	public static final String STRING_CONFIRMING = Messages.getString("monitor.confirming");
	
	/** 未確認（文字列）。 */
	public static final String STRING_UNCONFIRMED = Messages.getString("monitor.unconfirmed");

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
		} else if (type == ConfirmConstant.TYPE_CONFIRMING) {
			return STRING_CONFIRMING;
		}
		return "";
	}
}