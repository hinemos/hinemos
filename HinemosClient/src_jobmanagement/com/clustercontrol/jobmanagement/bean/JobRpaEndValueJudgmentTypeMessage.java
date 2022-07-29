/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

import org.openapitools.client.model.JobRpaEndValueConditionInfoResponse.ConditionTypeEnum;

import com.clustercontrol.util.Messages;

public class JobRpaEndValueJudgmentTypeMessage {
	/** ファイル */
	public static final String STRING_FILE = Messages.getString("file");
	/** リターンコード */
	public static final String STRING_RETURN_CODE = Messages.getString("return.code");

	/**
	 * 種別(enum)からMessageに変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(String enumValue) {
		if (enumValue.equals(ConditionTypeEnum.LOG.getValue())) {
			return STRING_FILE;
		}
		if (enumValue.equals(ConditionTypeEnum.RETURN_CODE.getValue())) {
			return STRING_RETURN_CODE;
		}
		return "";
	}

}
