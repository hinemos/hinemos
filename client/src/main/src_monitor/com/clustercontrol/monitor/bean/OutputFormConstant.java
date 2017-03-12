/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
