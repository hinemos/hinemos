/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.bean;

import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.util.Messages;

public class PriChangeFailTypeMessage {
	/** 重要度変化しない。 */ 
	public static final String STRING_NOT_PRIORITY_CHANGE = Messages.getString("notify.priority.change.none.fail");

	/** 重要度変化する。 */
	public static final String STRING_PRIORITY_CHANGE = Messages.getString("notify.priority.change.occur.fail");

	/**
	 * 種別から文字列に変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static String typeEnumToString(MonitorInfoResponse.PriorityChangeFailureTypeEnum type) {
		if (type == MonitorInfoResponse.PriorityChangeFailureTypeEnum.NOT_PRIORITY_CHANGE ) {
			return STRING_NOT_PRIORITY_CHANGE;
		} else if (type == MonitorInfoResponse.PriorityChangeFailureTypeEnum.PRIORITY_CHANGE) {
			return STRING_PRIORITY_CHANGE;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static MonitorInfoResponse.PriorityChangeFailureTypeEnum stringToTypeEnum(String string) {
		if (string.equals(STRING_NOT_PRIORITY_CHANGE)) {
			return MonitorInfoResponse.PriorityChangeFailureTypeEnum.NOT_PRIORITY_CHANGE;
		} else if (string.equals(STRING_PRIORITY_CHANGE)) {
			return MonitorInfoResponse.PriorityChangeFailureTypeEnum.PRIORITY_CHANGE;
		}
		return null;
	}
	

}
