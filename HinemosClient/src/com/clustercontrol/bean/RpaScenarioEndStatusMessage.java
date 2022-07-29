/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import org.openapitools.client.model.RpaScenarioOperationResultWithDetailResponse;

import com.clustercontrol.util.Messages;

/**
 * RPAシナリオ実行状態の定数クラス
 */
public class RpaScenarioEndStatusMessage {
	/** 終了(正常) */
	public static final String STRING_NORMAL_END = Messages.getString("view.rpa.scenario.operation.result.search.normal.end");

	/** 終了(異常) */
	public static final String STRING_ERROR_END = Messages.getString("view.rpa.scenario.operation.result.search.error.end");

	/** 実行中(正常) */
	public static final String STRING_NORMAL_RUNNING = Messages.getString("view.rpa.scenario.operation.result.search.normal.running");

	/** 実行中(異常) */
	public static final String STRING_ERROR_RUNNING = Messages.getString("view.rpa.scenario.operation.result.search.error.running");

	/** 不明 */
	public static final String STRING_UNKNOWN = Messages.getString("view.rpa.scenario.operation.result.search.unknown");

	/**
	 * 種別(enum)から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeEnumValueToString(String enumValue) {
		if (enumValue.equals(RpaScenarioOperationResultWithDetailResponse.StatusEnum.NORMAL_END.getValue())) {
			return STRING_NORMAL_END;
		} else if (enumValue.equals(RpaScenarioOperationResultWithDetailResponse.StatusEnum.NORMAL_RUNNING.getValue())) {
			return STRING_NORMAL_RUNNING;
		} else if (enumValue.equals(RpaScenarioOperationResultWithDetailResponse.StatusEnum.ERROR_END.getValue())) {
			return STRING_ERROR_END;
		} else if (enumValue.equals(RpaScenarioOperationResultWithDetailResponse.StatusEnum.ERROR_RUNNING.getValue())) {
			return STRING_ERROR_RUNNING;
		} else if (enumValue.equals(RpaScenarioOperationResultWithDetailResponse.StatusEnum.UNKNOWN.getValue())) {
			return STRING_UNKNOWN;
		}
		return "";
	}
	
	/**
	 * 文字列から種別(enum)に変換します。<BR>
	 * 
	 * @param string
	 * @return typeEnumValue
	 */
	public static String stringTotypeEnumValue(String string) {
		if (string.equals(STRING_NORMAL_END)) {
			return RpaScenarioOperationResultWithDetailResponse.StatusEnum.NORMAL_END.getValue();
		} else if (string.equals(STRING_NORMAL_RUNNING)) {
			return RpaScenarioOperationResultWithDetailResponse.StatusEnum.NORMAL_RUNNING.getValue();
		} else if (string.equals(STRING_ERROR_END)) {
			return RpaScenarioOperationResultWithDetailResponse.StatusEnum.ERROR_END.getValue();
		} else if (string.equals(STRING_ERROR_RUNNING)) {
			return RpaScenarioOperationResultWithDetailResponse.StatusEnum.ERROR_RUNNING.getValue();
		} else if (string.equalsIgnoreCase(STRING_UNKNOWN)) {
			return RpaScenarioOperationResultWithDetailResponse.StatusEnum.UNKNOWN.getValue();
		}
		return "";
	}
}