/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

/**
 * イベント情報の確認状態の定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConfirmConstant {
	/** 確認（種別）。 */
	public static final int TYPE_CONFIRMED = 1;

	/** 未確認（種別）。 */
	public static final int TYPE_UNCONFIRMED = 0;

	/** 確認と未確認 */
	public static final int TYPE_ALL = -1;

	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessageCode(int type) {
		if (type == TYPE_CONFIRMED) {
			return "TYPE_CONFIRMED";
		} else if (type == TYPE_UNCONFIRMED) {
			return "TYPE_UNCONFIRMED";
		}
		return "";
	}
}