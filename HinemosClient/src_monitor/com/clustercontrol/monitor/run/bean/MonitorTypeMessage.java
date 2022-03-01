/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.bean;

import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.util.Messages;

/**
 * 監視種別の定義を定数として格納するクラス<BR>
 * 
 * @version 4.0.0
 * @since 2.1.0
 */
public class MonitorTypeMessage {
	/** 真偽値（文字列）。 */
	public static final String STRING_TRUTH = Messages.getString("truth");

	/** 数値（文字列）。 */
	public static final String STRING_NUMERIC = Messages.getString("numeric");

	/** 文字列（文字列）。 */
	public static final String STRING_STRING = Messages.getString("string");

	/** トラップ（文字列）。 */
	public static final String STRING_TRAP = Messages.getString("trap");

	/** シナリオ（文字列）。 */
	public static final String STRING_SCENARIO = Messages.getString("scenario");
	
	/** バイナリ（文字列）。 */
	public static final String STRING_BINARY = Messages.getString("binary");
	
	/**
	 * 種別から文字列に変換します。
	 * 
	 * @param type 種別
	 * @return 文字列
	 */
	public static String typeToString(int type) {
		if (type == MonitorTypeConstant.TYPE_TRUTH) {
			return STRING_TRUTH;
		} else if (type == MonitorTypeConstant.TYPE_NUMERIC) {
			return STRING_NUMERIC;
		} else if (type == MonitorTypeConstant.TYPE_STRING) {
			return STRING_STRING;
		} else if (type == MonitorTypeConstant.TYPE_TRAP) {
			return STRING_TRAP;
		} else if (type == MonitorTypeConstant.TYPE_SCENARIO) {
			return STRING_SCENARIO;
		} else if (type == MonitorTypeConstant.TYPE_BINARY) {
			return STRING_BINARY;
		}
		return "";
	}

	/**
	 * 種別から文字列に変換します。
	 * 
	 * @param code MonitorTypeEnumのコード値
	 * @return 文字列
	 */
	public static String codeToString(String code) {
		if (code.equals(MonitorInfoResponse.MonitorTypeEnum.TRUTH.getValue())) {
			return STRING_TRUTH;
		} else if (code.equals(MonitorInfoResponse.MonitorTypeEnum.NUMERIC.getValue())) {
			return STRING_NUMERIC;
		} else if (code.equals(MonitorInfoResponse.MonitorTypeEnum.STRING.getValue())) {
			return STRING_STRING;
		} else if (code.equals(MonitorInfoResponse.MonitorTypeEnum.TRAP.getValue())) {
			return STRING_TRAP;
		} else if (code.equals(MonitorInfoResponse.MonitorTypeEnum.SCENARIO.getValue())) {
			return STRING_SCENARIO;
		} else if (code.equals(MonitorInfoResponse.MonitorTypeEnum.BINARY.getValue())) {
			return STRING_BINARY;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。
	 * 
	 * @param string 文字列
	 * @return 種別
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_TRUTH)) {
			return MonitorTypeConstant.TYPE_TRUTH;
		} else if (string.equals(STRING_NUMERIC)) {
			return MonitorTypeConstant.TYPE_NUMERIC;
		} else if (string.equals(STRING_STRING)) {
			return MonitorTypeConstant.TYPE_STRING;
		} else if (string.equals(STRING_TRAP)) {
			return MonitorTypeConstant.TYPE_TRAP;
		} else if (string.equals(STRING_SCENARIO)) {
			return MonitorTypeConstant.TYPE_SCENARIO;
		} else if (string.equals(STRING_BINARY)) {
			return MonitorTypeConstant.TYPE_BINARY;
		}
		return -1;
	}
}