/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

import org.openapitools.client.model.JobRpaEndValueConditionInfoResponse.ReturnCodeConditionEnum;

import com.clustercontrol.util.Messages;

public class JobRpaReturnCodeConditionMessage {

	/** = */
	public static final String STRING_EQUAL = Messages.getString("wait.rule.decision.condition.equal");
	/** != */
	public static final String STRING_NOT_EQUAL = Messages.getString("wait.rule.decision.condition.not.equal");
	/** > */
	public static final String STRING_GREATER_THAN = Messages.getString("wait.rule.decision.condition.greater.than");
	/** >= */
	public static final String STRING_GREATER_THAN_OR_EQUAL_TO = Messages
			.getString("wait.rule.decision.condition.greater.than.or.equal.to");
	/** < */
	public static final String STRING_LESS_THAN = Messages.getString("wait.rule.decision.condition.less.than");
	/** <= */
	public static final String STRING_LESS_THAN_OR_EQUAL_TO = Messages
			.getString("wait.rule.decision.condition.less.than.or.equal.to");

	/**
	 * 種別(enum)からMessageに変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(String enumValue) {
		if (enumValue.equals(ReturnCodeConditionEnum.EQUAL_NUMERIC.getValue())) {
			return STRING_EQUAL;
		} else if (enumValue.equals(ReturnCodeConditionEnum.NOT_EQUAL_NUMERIC.getValue())) {
			return STRING_NOT_EQUAL;
		} else if (enumValue.equals(ReturnCodeConditionEnum.GREATER_THAN.getValue())) {
			return STRING_GREATER_THAN;
		} else if (enumValue.equals(ReturnCodeConditionEnum.GREATER_THAN_OR_EQUAL_TO.getValue())) {
			return STRING_GREATER_THAN_OR_EQUAL_TO;
		} else if (enumValue.equals(ReturnCodeConditionEnum.LESS_THAN.getValue())) {
			return STRING_LESS_THAN;
		} else if (enumValue.equals(ReturnCodeConditionEnum.LESS_THAN_OR_EQUAL_TO.getValue())) {
			return STRING_LESS_THAN_OR_EQUAL_TO;
		}
		return "";
	}
}
