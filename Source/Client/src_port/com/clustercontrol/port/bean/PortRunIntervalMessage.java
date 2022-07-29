/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.port.bean;

import com.clustercontrol.util.Messages;

/**
 * port実行間隔の定義を定数として格納するクラス<BR>
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class PortRunIntervalMessage {
	/** 1秒（文字列）。 */
	public static final String STRING_SEC_01 = PortRunIntervalConstant.TYPE_SEC_01/1000 + Messages.getString("second");

	/** 2秒（文字列）。 */
	public static final String STRING_SEC_02 = PortRunIntervalConstant.TYPE_SEC_02/1000 + Messages.getString("second");

	/** 5秒（文字列）。 */
	public static final String STRING_SEC_05 = PortRunIntervalConstant.TYPE_SEC_05/1000 + Messages.getString("second");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == PortRunIntervalConstant.TYPE_SEC_01) {
			return STRING_SEC_01;
		} else if (type == PortRunIntervalConstant.TYPE_SEC_02) {
			return STRING_SEC_02;
		} else if (type == PortRunIntervalConstant.TYPE_SEC_05) {
			return STRING_SEC_05;
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
		if (string.equals(STRING_SEC_01)) {
			return PortRunIntervalConstant.TYPE_SEC_01;
		} else if (string.equals(STRING_SEC_02)) {
			return PortRunIntervalConstant.TYPE_SEC_02;
		} else if (string.equals(STRING_SEC_05)) {
			return PortRunIntervalConstant.TYPE_SEC_05;
		}
		return -1;
	}
}