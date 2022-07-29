/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import org.openapitools.client.model.JobDetailInfoResponse;
import com.clustercontrol.util.Messages;

/**
 * ジョブ終了状態の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class EndStatusMessage {
	/** 正常 */
	public static final String STRING_NORMAL = Messages.getString("normal");

	/** 警告 */
	public static final String STRING_WARNING = Messages.getString("warning");

	/** 異常 */
	public static final String STRING_ABNORMAL = Messages.getString("abnormal");

	/** 開始 */
	public static final String STRING_BEGINNING = Messages.getString("start");

	/** すべての終了状態 */
	public static final String STRING_ANY = Messages.getString("asterisk");
	
	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == EndStatusConstant.TYPE_NORMAL) {
			return STRING_NORMAL;
		} else if (type == EndStatusConstant.TYPE_WARNING) {
			return STRING_WARNING;
		} else if (type == EndStatusConstant.TYPE_ABNORMAL) {
			return STRING_ABNORMAL;
		} else if (type == EndStatusConstant.TYPE_BEGINNING) {
			return STRING_BEGINNING;
		} else if (type == EndStatusConstant.TYPE_ANY) {
			return STRING_ANY;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_NORMAL)) {
			return EndStatusConstant.TYPE_NORMAL;
		} else if (string.equals(STRING_WARNING)) {
			return EndStatusConstant.TYPE_WARNING;
		} else if (string.equals(STRING_ABNORMAL)) {
			return EndStatusConstant.TYPE_ABNORMAL;
		} else if (string.equals(STRING_BEGINNING)) {
			return EndStatusConstant.TYPE_BEGINNING;
		} else if (string.equalsIgnoreCase(STRING_ANY)) {
			return EndStatusConstant.TYPE_ANY;
		}
		return -1;
	}

	/**
	 * 種別(enum)から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeEnumValueToString(String enumValue) {
		if (enumValue.equals(JobDetailInfoResponse.EndStatusEnum.NORMAL.getValue())) {
			return STRING_NORMAL;
		} else if (enumValue.equals(JobDetailInfoResponse.EndStatusEnum.WARNING.getValue())) {
			return STRING_WARNING;
		} else if (enumValue.equals(JobDetailInfoResponse.EndStatusEnum.ABNORMAL.getValue())) {
			return STRING_ABNORMAL;
		} else if (enumValue.equals(JobDetailInfoResponse.EndStatusEnum.BEGINNING.getValue())) {
			return STRING_BEGINNING;
		} else if (enumValue.equals(JobDetailInfoResponse.EndStatusEnum.ANY.getValue())) {
			return STRING_ANY;
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
		if (string.equals(STRING_NORMAL)) {
			return JobDetailInfoResponse.EndStatusEnum.NORMAL.getValue();
		} else if (string.equals(STRING_WARNING)) {
			return JobDetailInfoResponse.EndStatusEnum.WARNING.getValue();
		} else if (string.equals(STRING_ABNORMAL)) {
			return JobDetailInfoResponse.EndStatusEnum.ABNORMAL.getValue();
		} else if (string.equals(STRING_BEGINNING)) {
			return JobDetailInfoResponse.EndStatusEnum.BEGINNING.getValue();
		} else if (string.equalsIgnoreCase(STRING_ANY)) {
			return JobDetailInfoResponse.EndStatusEnum.ANY.getValue();
		}
		return "";
	}	
}