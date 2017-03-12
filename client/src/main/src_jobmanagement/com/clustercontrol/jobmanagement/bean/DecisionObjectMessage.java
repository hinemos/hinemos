/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.bean;

import com.clustercontrol.util.Messages;

/**
 * 判定条件の定数クラス<BR>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class DecisionObjectMessage {

	/** =(数値) */
	public static final String STRING_EQUAL_NUMERIC = Messages.getString("wait.rule.decision.condition.equal")
			+ "(" + Messages.getString("numeric") + ")";

	/** !=(数値) */
	public static final String STRING_NOT_EQUAL_NUMERIC = Messages.getString("wait.rule.decision.condition.not.equal")
			+ "(" + Messages.getString("numeric") + ")";

	/** >(数値) */
	public static final String STRING_GREATER_THAN = Messages.getString("wait.rule.decision.condition.greater.than")
			+ "(" + Messages.getString("numeric") + ")";

	/** >=(数値) */
	public static final String STRING_GREATER_THAN_OR_EQUAL_TO = Messages.getString("wait.rule.decision.condition.greater.than.or.equal.to")
			+ "(" + Messages.getString("numeric") + ")";

	/** <(数値) */
	public static final String STRING_LESS_THAN = Messages.getString("wait.rule.decision.condition.less.than")
			+ "(" + Messages.getString("numeric") + ")";

	/** <=(数値) */
	public static final String STRING_LESS_THAN_OR_EQUAL_TO = Messages.getString("wait.rule.decision.condition.less.than.or.equal.to")
			+ "(" + Messages.getString("numeric") + ")";

	/** =(文字列) */
	public static final String STRING_EQUAL_STRING = Messages.getString("wait.rule.decision.condition.equal")
			+ "(" + Messages.getString("string") + ")";

	/** !=(文字列) */
	public static final String STRING_NOT_EQUAL_STRING = Messages.getString("wait.rule.decision.condition.not.equal")
			+ "(" + Messages.getString("string") + ")";

	/**
	 * 条件から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int condition) {
		if (condition == DecisionObjectConstant.EQUAL_NUMERIC) {
			return STRING_EQUAL_NUMERIC;
		}
		else if (condition == DecisionObjectConstant.NOT_EQUAL_NUMERIC) {
			return STRING_NOT_EQUAL_NUMERIC;
		}
		else if (condition == DecisionObjectConstant.GREATER_THAN) {
			return STRING_GREATER_THAN;
		}
		else if (condition == DecisionObjectConstant.GREATER_THAN_OR_EQUAL_TO) {
			return STRING_GREATER_THAN_OR_EQUAL_TO;
		}
		else if (condition == DecisionObjectConstant.LESS_THAN) {
			return STRING_LESS_THAN;
		}
		else if (condition == DecisionObjectConstant.LESS_THAN_OR_EQUAL_TO) {
			return STRING_LESS_THAN_OR_EQUAL_TO;
		}
		else if (condition == DecisionObjectConstant.EQUAL_STRING) {
			return STRING_EQUAL_STRING;
		}
		else if (condition == DecisionObjectConstant.NOT_EQUAL_STRING) {
			return STRING_NOT_EQUAL_STRING;
		}
		return "";
	}

	/**
	 * 文字列から条件に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_EQUAL_NUMERIC)) {
			return DecisionObjectConstant.EQUAL_NUMERIC;
		}
		else if (string.equals(STRING_NOT_EQUAL_NUMERIC)) {
			return DecisionObjectConstant.NOT_EQUAL_NUMERIC;
		}
		else if (string.equals(STRING_GREATER_THAN)) {
			return DecisionObjectConstant.GREATER_THAN;
		}
		else if (string.equals(STRING_GREATER_THAN_OR_EQUAL_TO)) {
			return DecisionObjectConstant.GREATER_THAN_OR_EQUAL_TO;
		}
		else if (string.equals(STRING_LESS_THAN)) {
			return DecisionObjectConstant.LESS_THAN;
		}
		else if (string.equals(STRING_LESS_THAN_OR_EQUAL_TO)) {
			return DecisionObjectConstant.LESS_THAN_OR_EQUAL_TO;
		}
		else if (string.equals(STRING_EQUAL_STRING)) {
			return DecisionObjectConstant.EQUAL_STRING;
		}
		else if (string.equals(STRING_NOT_EQUAL_STRING)) {
			return DecisionObjectConstant.NOT_EQUAL_STRING;
		}
		return -1;
	}
}