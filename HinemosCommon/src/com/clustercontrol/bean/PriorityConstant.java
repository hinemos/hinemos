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

package com.clustercontrol.bean;


/**
 * 重要度の定義を定数として格納するクラス<BR>
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
public class PriorityConstant {

	/** 危険（種別）。 */
	public static final int TYPE_CRITICAL = 0;

	/** 警告（種別）。 */
	public static final int TYPE_WARNING = 2;

	/** 通知（種別）。 */
	public static final int TYPE_INFO = 3;

	/** 不明（種別）。 */
	public static final int TYPE_UNKNOWN = 1;

	/** なし（種別）。 */
	public static final int TYPE_NONE = 4;

	/** 値取得失敗 */
	public static final int TYPE_FAILURE = -1;

	/** 重要度のリスト（重要度の高いもの順） **/
	public static int[] PRIORITY_LIST = {
		TYPE_CRITICAL,
		TYPE_UNKNOWN,
		TYPE_WARNING,
		TYPE_INFO,
	};
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessageCode(int type) {
		if (type == TYPE_CRITICAL) {
			return "CRITICAL";
		} else if (type == TYPE_WARNING) {
			return "WARNING";
		} else if (type == TYPE_INFO) {
			return "INFO";
		} else if (type == TYPE_UNKNOWN) {
			return "UNKNOWN";
		} else if (type == TYPE_NONE) {
			return "";
		}
		return "";
	}
	
}