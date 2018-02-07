/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import com.clustercontrol.util.Messages;

/**
 * 処理の定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProcessMessage {
	/** 処理する（文字列）。 */
	public static final String STRING_YES = Messages.getString("yes");

	/** 処理しない（文字列）。 */
	public static final String STRING_NO = Messages.getString("no");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(boolean type) {
		if (type) {
			return STRING_YES;
		} else {
			return STRING_NO;
		}
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static boolean stringToType(String string) {
		if (string.equals(STRING_YES)) {
			return true;
		} else {
			return false;
		}
	}
}