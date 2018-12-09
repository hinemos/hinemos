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
 * ファイル出力形式の定義を定数として格納するクラス<BR>
 * 
 * @version 2.1.0
 * @since 2.1.0
 */
public class OutputFormConstant {
	/** CSV（種別）。 */
	public static final int TYPE_CSV = 1;

	/** PDF（種別）。 */
	public static final int TYPE_PDF = 0;

	/** CSV（文字列）。 */
	public static final String STRING_CSV = Messages.getString("csv");

	/** PDF（文字列）。 */
	public static final String STRING_PDF = Messages.getString("pdf");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == TYPE_CSV) {
			return STRING_CSV;
		} else if (type == TYPE_PDF) {
			return STRING_PDF;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_CSV)) {
			return TYPE_CSV;
		} else if (string.equals(STRING_PDF)) {
			return TYPE_PDF;
		}
		return -1;
	}
}
