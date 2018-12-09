/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import com.clustercontrol.util.Messages;

/**
 * 対象ファシリティ種別のクラス<BR>
 * 
 * ファシリティ情報を取得する際に指定する種別を定義しているクラス
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class FacilityTargetMessage {
	/** 直下（対象ファシリティの種別） */
	public static final String STRING_BENEATH = Messages
			.getString("facility.target.beneath");

	/** 配下全て（対象ファシリティの種別） */
	public static final String STRING_ALL = Messages
			.getString("facility.target.all");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == FacilityTargetConstant.TYPE_BENEATH) {
			return STRING_BENEATH;
		} else if (type == FacilityTargetConstant.TYPE_ALL) {
			return STRING_ALL;
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
		if (string.equals(STRING_BENEATH)) {
			return FacilityTargetConstant.TYPE_BENEATH;
		} else if (string.equals(STRING_ALL)) {
			return FacilityTargetConstant.TYPE_ALL;
		}
		return -1;
	}
}