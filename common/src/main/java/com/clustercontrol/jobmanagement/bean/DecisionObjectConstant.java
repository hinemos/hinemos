/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

/**
 * 判定条件の定数クラス<BR>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class DecisionObjectConstant {
	/** =(数値) */
	public static final int EQUAL_NUMERIC = 0;

	/** !=(数値) */
	public static final int NOT_EQUAL_NUMERIC = 1;

	/** >(数値) */
	public static final int GREATER_THAN = 2;

	/** >=(数値) */
	public static final int GREATER_THAN_OR_EQUAL_TO = 3;

	/** <(数値) */
	public static final int LESS_THAN = 4;

	/** <=(数値) */
	public static final int LESS_THAN_OR_EQUAL_TO = 5;

	/** =(文字列) */
	public static final int EQUAL_STRING = 6;

	/** !=(文字列) */
	public static final int NOT_EQUAL_STRING = 7;

	private DecisionObjectConstant() {
		throw new IllegalStateException("ConstClass");
	}
}