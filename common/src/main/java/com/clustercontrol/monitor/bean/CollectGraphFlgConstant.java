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

package com.clustercontrol.monitor.bean;

/**
 * イベント情報の性能グラフ用フラグの定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class CollectGraphFlgConstant {
	/** ON */
	public static final Boolean TYPE_ON = Boolean.TRUE;

	/** OFF */
	public static final Boolean TYPE_OFF = Boolean.FALSE;

	/** ONとOFF */
	public static final Boolean TYPE_ALL = null;

	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessageCode(boolean type) {
		if (type == TYPE_ON) {
			return "TYPE_ON";
		} else if (type == TYPE_OFF) {
			return "TYPE_OFF";
		}
		return "";
	}
}