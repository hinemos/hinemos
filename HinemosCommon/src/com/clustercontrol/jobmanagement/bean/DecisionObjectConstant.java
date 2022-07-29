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

	/** IN(数値) */
	public static final int IN_NUMERIC = 8;

	/** NOT IN(数値) */
	public static final int NOT_IN_NUMERIC = 9;

	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessage(int type) {
		if (type == EQUAL_NUMERIC) {
			return "EQUAL_NUMERIC";
		} else if (type == NOT_EQUAL_NUMERIC) {
			return "NOT_EQUAL_NUMERIC";
		} else if (type == GREATER_THAN) {
			return "GREATER_THAN";
		} else if (type == GREATER_THAN_OR_EQUAL_TO) {
			return "GREATER_THAN_OR_EQUAL_TO";
		} else if (type == LESS_THAN) {
			return "LESS_THAN";
		} else if (type == LESS_THAN_OR_EQUAL_TO) {
			return "LESS_THAN_OR_EQUAL_TO";
		} else if (type == EQUAL_STRING) {
			return "EQUAL_STRING";
		} else if (type == NOT_EQUAL_STRING) {
			return "NOT_EQUAL_STRING";
		} else if (type == IN_NUMERIC) {
			return "IN_NUMERIC";
		} else if (type == NOT_IN_NUMERIC) {
			return "NOT_IN_NUMERIC";
		}
		return null;
	}
}