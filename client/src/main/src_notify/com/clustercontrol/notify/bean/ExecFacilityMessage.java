/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.bean;

import com.clustercontrol.util.Messages;

/**
 * 対象ファシリティの定義を定数として格納するクラス<BR>
 * 
 * @version 3.0.0
 * @since 3.0.0
 */
public class ExecFacilityMessage {
	/** イベント発生ノード（文字列）。 */
	public static final String STRING_GENERATION = Messages.getString("notify.node.generation");

	/** 固定スコープ（文字列）。 */
	public static final String STRING_FIX = Messages.getString("notify.node.fix");

	/**
	 * 種別から文字列に変換します。
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == ExecFacilityConstant.TYPE_GENERATION) {
			return STRING_GENERATION;
		} else if (type == ExecFacilityConstant.TYPE_FIX) {
			return STRING_FIX;
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
		if (string.equals(STRING_GENERATION)) {
			return ExecFacilityConstant.TYPE_GENERATION;
		} else if (string.equals(STRING_FIX)) {
			return ExecFacilityConstant.TYPE_FIX;
		}
		return -1;
	}

}