/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import com.clustercontrol.util.Messages;

public class FacilityMessage {

	/** ----------------------- */
	/** ----- ファシリティ関連 ----- */
	/** ----------------------- */

	/** スコープ（ファシリティの種別） */
	public static final String STRING_SCOPE = Messages.getString("scope");

	/** ノード（ファシリティの種別） */
	public static final String STRING_NODE = Messages.getString("node");

	/**
	 * 種別から文字列に変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == FacilityConstant.TYPE_COMPOSITE) {
			return FacilityConstant.STRING_COMPOSITE;
		} else if (type == FacilityConstant.TYPE_SCOPE) {
			return STRING_SCOPE;
		} else if (type == FacilityConstant.TYPE_NODE) {
			return STRING_NODE;
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
		if (string.equals(FacilityConstant.STRING_COMPOSITE)) {
			return FacilityConstant.TYPE_COMPOSITE;
		} else if (string.equals(STRING_SCOPE)) {
			return FacilityConstant.TYPE_SCOPE;
		} else if (string.equals(STRING_NODE)) {
			return FacilityConstant.TYPE_NODE;
		}
		return -1;
	}

}
