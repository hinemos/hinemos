/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.bean;

import com.clustercontrol.util.Messages;

/**
 * 稼動／非稼動の定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class OperateConstant {
	/** 稼動（種別）。 */
	public static final int TYPE_OPERATE = 1;

	/** 非稼動（種別）。 */
	public static final int TYPE_NONOPERATE = 0;

	/** 稼動（文字列）。 */
	public static final String STRING_OPERATE = Messages.getString("calendar.detail.operation.1");

	/** 非稼動（文字列）。 */
	public static final String STRING_NONOPERATE = Messages.getString("calendar.detail.operation.2");

	/** 稼動（真偽）。 */
	public static final boolean BOOLEAN_OPERATE = true;

	/** 非稼動（真偽）。 */
	public static final boolean BOOLEAN_NONOPERATE = false;

	/**
	 * 種別から文字列に変換します。
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == TYPE_OPERATE) {
			return STRING_OPERATE;
		} else if (type == TYPE_NONOPERATE) {
			return STRING_NONOPERATE;
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
		if (string.equals(STRING_OPERATE)) {
			return TYPE_OPERATE;
		} else if (string.equals(STRING_NONOPERATE)) {
			return TYPE_NONOPERATE;
		}
		return -1;
	}

	/**
	 * 種別から真偽に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 真偽
	 */
	public static boolean typeToBoolean(int type) {
		if (type == TYPE_OPERATE) {
			return BOOLEAN_OPERATE;
		} else if (type == TYPE_NONOPERATE) {
			return BOOLEAN_NONOPERATE;
		}
		return false;
	}

	/**
	 * 真偽から種別に変換します。<BR>
	 * 
	 * @param bool 真偽
	 * @return 種別
	 */
	public static int booleanToType(boolean bool) {
		if (bool == BOOLEAN_OPERATE) {
			return TYPE_OPERATE;
		} else {
			return TYPE_NONOPERATE;
		}
	}
}
