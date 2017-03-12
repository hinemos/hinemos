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

package com.clustercontrol.performance.bean;

import com.clustercontrol.util.Messages;

/**
 * 実績収集の状態の定数クラス
 *
 * @version 4.0.0
 * @since 4.0.0
 *
 */
public class PerformanceStatusConstant {

	/** 収集中(文字列) */
	public static final String STRING_RUNNING = Messages.getString("collection.running");

	/** 停止中(文字列) */
	public static final String STRING_STOP = Messages.getString("collection.stop");


	public static boolean stringToType(String type) {
		if (type.equals(STRING_RUNNING)) {
			return true;
		} else {
			return false;
		}
	}

	public static String typeToString(boolean type) {
		if (type) {
			return STRING_RUNNING;
		} else {
			return STRING_STOP;
		}
	}
}
