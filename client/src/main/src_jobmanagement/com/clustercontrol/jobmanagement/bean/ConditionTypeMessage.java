/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import com.clustercontrol.util.Messages;

/**
 * ジョブの待ち条件の種別の定数を定義するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConditionTypeMessage {

	/** AND条件（文字列） */
	public static final String STRING_AND = Messages.getString("and");

	/** OR条件（文字列） */
	public static final String STRING_OR = Messages.getString("or");

	/**
	 * 条件（数値）から条件（文字列）に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return ジョブの待ち条件を示す文字列
	 */
	public static String typeToString(int type) {
		if (type == ConditionTypeConstant.TYPE_AND) {
			return STRING_AND;
		} else if (type == ConditionTypeConstant.TYPE_OR) {
			return STRING_OR;
		}
		return "";
	}

	/**
	 * 条件（文字列）から条件（数値）に変換します。<BR>
	 * 
	 * @param string 文字列
	 * @return ジョブの待ち条件を示す数値
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_AND)) {
			return ConditionTypeConstant.TYPE_AND;
		} else if (string.equals(STRING_OR)) {
			return ConditionTypeConstant.TYPE_OR;
		}
		return -1;
	}
}