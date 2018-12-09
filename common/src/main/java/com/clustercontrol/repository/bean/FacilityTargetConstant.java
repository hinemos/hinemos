/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

/**
 * 対象ファシリティ種別のクラス<BR>
 * 
 * ファシリティ情報を取得する際に指定する種別を定義しているクラス
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class FacilityTargetConstant {
	/** 直下（対象ファシリティの種別） */
	public static final int TYPE_BENEATH = 0;

	/** 配下全て（対象ファシリティの種別） */
	public static final int TYPE_ALL = 1;
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessageCode(int type) {
		if (type == TYPE_BENEATH) {
			return "TYPE_BENEATH";
		} else if (type == TYPE_ALL) {
			return "TYPE_ALL";
		}
		return "";
	}

	private FacilityTargetConstant() {
		throw new IllegalStateException("ConstClass");
	}
}