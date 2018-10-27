/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

/**
 * イベント情報の性能グラフ用フラグの定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class CollectGraphFlgConstant {
	/** ON */
	public static final Boolean TYPE_ON = Boolean.TRUE;

	/** OFF */
	public static final Boolean TYPE_OFF = Boolean.FALSE;

	/** ONとOFF */
	public static final Boolean TYPE_ALL = null;

	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessageCode(boolean type) {
		if (type == TYPE_ON) {
			return "TYPE_ON";
		} else if (type == TYPE_OFF) {
			return "TYPE_OFF";
		}
		return "";
	}
}