/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
