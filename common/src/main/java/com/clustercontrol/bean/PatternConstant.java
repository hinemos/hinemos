/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

/**
 * パターンマッチの定数クラス<BR>
 *
 * @version 5.0.3
 * @since 5.0.3
 */
public class PatternConstant {
	public static final String HINEMOS_ID_PATTERN = "^[A-Za-z0-9-_.@]+$";

	private PatternConstant() {
		throw new IllegalStateException("ConstClass");
	}
}