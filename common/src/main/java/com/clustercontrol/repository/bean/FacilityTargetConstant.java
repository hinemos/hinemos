/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
}