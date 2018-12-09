/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.constant;

/**
 * YES／NOの定義を定数として格納するクラス<BR>
 * 
 * @version 6.0.0
 * @since 1.0.0
 */
public class YesNoConstant {
	/** Yes（種別）。 */
	private static final int TYPE_YES = 1;

	/** No（種別）。 */
	private static final int TYPE_NO = 0;

	/** Yes（真偽）。 */
	public static final boolean BOOLEAN_YES = true;

	/** No（真偽）。 */
	public static final boolean BOOLEAN_NO = false;

	/**
	 * 種別から真偽に変換します。
	 * 
	 * @param type 種別
	 * @return 真偽
	 */
	public static boolean typeToBoolean(int type) {
		if (type == TYPE_YES) {
			return BOOLEAN_YES;
		} else if (type == TYPE_NO) {
			return BOOLEAN_NO;
		}
		return false;
	}

	/**
	 * 真偽から種別に変換します。
	 * 
	 * @param bool 真偽
	 * @return 種別
	 */
	public static int booleanToType(boolean bool) {
		if (bool) {
			return TYPE_YES;
		}
		return TYPE_NO;
	}
}