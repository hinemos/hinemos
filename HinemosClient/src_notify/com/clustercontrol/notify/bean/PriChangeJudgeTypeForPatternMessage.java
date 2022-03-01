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

public class PriChangeJudgeTypeForPatternMessage {
	/** 重要度変化しない。 */ 
	public static final String STRING_NOT_PRIORITY_CHANGE = Messages.getString("notify.priority.change.none");

	/** パターンマッチをまたいて重要度変化する。 */
	public static final String STRING_ACROSS_MONITOR_DETAIL_ID = Messages.getString("notify.priority.change.occur.pattern");

	
	/**
	 * 種別から文字列に変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static String typeEnumToString(MonitorInfoResponse.PriorityChangeJudgmentTypeEnum type) {
		if (type == MonitorInfoResponse.PriorityChangeJudgmentTypeEnum.NOT_PRIORITY_CHANGE) {
			return STRING_NOT_PRIORITY_CHANGE;
		} else if (type == MonitorInfoResponse.PriorityChangeJudgmentTypeEnum.ACROSS_MONITOR_DETAIL_ID) {
			return STRING_ACROSS_MONITOR_DETAIL_ID;
		}
		return "";
	}
	
	/**
	 * 文字列から種別に変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static MonitorInfoResponse.PriorityChangeJudgmentTypeEnum stringToTypeEnum(String string) {
		if (string.equals(STRING_NOT_PRIORITY_CHANGE)) {
			return MonitorInfoResponse.PriorityChangeJudgmentTypeEnum.NOT_PRIORITY_CHANGE;
		} else if (string.equals(STRING_ACROSS_MONITOR_DETAIL_ID)) {
			return MonitorInfoResponse.PriorityChangeJudgmentTypeEnum.ACROSS_MONITOR_DETAIL_ID;
		}
		return null;
	}
	
}
