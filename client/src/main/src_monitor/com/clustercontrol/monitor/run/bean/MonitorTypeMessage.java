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

package com.clustercontrol.monitor.run.bean;

import com.clustercontrol.util.Messages;

/**
 * 監視種別の定義を定数として格納するクラス<BR>
 * 
 * @version 4.0.0
 * @since 2.1.0
 */
public class MonitorTypeMessage {
	/** 真偽値（文字列）。 */
	public static final String STRING_TRUTH = Messages.getString("truth");

	/** 数値（文字列）。 */
	public static final String STRING_NUMERIC = Messages.getString("numeric");

	/** 文字列（文字列）。 */
	public static final String STRING_STRING = Messages.getString("string");

	/** トラップ（文字列）。 */
	public static final String STRING_TRAP = Messages.getString("trap");

	/** シナリオ（文字列）。 */
	public static final String STRING_SCENARIO = Messages.getString("scenario");
	
	/**
	 * 種別から文字列に変換します。
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == MonitorTypeConstant.TYPE_TRUTH) {
			return STRING_TRUTH;
		} else if (type == MonitorTypeConstant.TYPE_NUMERIC) {
			return STRING_NUMERIC;
		} else if (type == MonitorTypeConstant.TYPE_STRING) {
			return STRING_STRING;
		} else if (type == MonitorTypeConstant.TYPE_TRAP) {
			return STRING_TRAP;
		} else if (type == MonitorTypeConstant.TYPE_SCENARIO) {
			return STRING_SCENARIO;
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
		if (string.equals(STRING_TRUTH)) {
			return MonitorTypeConstant.TYPE_TRUTH;
		} else if (string.equals(STRING_NUMERIC)) {
			return MonitorTypeConstant.TYPE_NUMERIC;
		} else if (string.equals(STRING_STRING)) {
			return MonitorTypeConstant.TYPE_STRING;
		} else if (string.equals(STRING_TRAP)) {
			return MonitorTypeConstant.TYPE_TRAP;
		} else if (string.equals(STRING_SCENARIO)) {
			return MonitorTypeConstant.TYPE_SCENARIO;
		}
		return -1;
	}
}