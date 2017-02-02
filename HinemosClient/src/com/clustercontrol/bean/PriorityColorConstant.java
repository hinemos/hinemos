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

import org.eclipse.swt.graphics.Color;

/**
 * 重要度背景色の定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class PriorityColorConstant extends PriorityConstant {
	/** 危険（色）。 */
	public static final Color COLOR_CRITICAL = new Color(null, 255, 0, 0);

	/** 警告（色）。 */
	public static final Color COLOR_WARNING = new Color(null, 255, 255, 0);

	/** 通知（色）。 */
	public static final Color COLOR_INFO = new Color(null, 0, 255, 0);

	/** 不明（色）。 */
	public static final Color COLOR_UNKNOWN = new Color(null, 128, 192, 255);

	/** なし（色）。 */
	public static final Color COLOR_NONE = new Color(null, 255, 255, 255);

	/**
	 * 種別から色に変換します。<BR>
	 * 
	 * @param type 種別
	 * @return 色
	 */
	public static Color typeToColor(int type) {
		if (type == TYPE_CRITICAL) {
			return COLOR_CRITICAL;
		} else if (type == TYPE_WARNING) {
			return COLOR_WARNING;
		} else if (type == TYPE_INFO) {
			return COLOR_INFO;
		} else if (type == TYPE_UNKNOWN) {
			return COLOR_UNKNOWN;
		} else if (type == TYPE_NONE) {
			return COLOR_NONE;
		}
		return new Color(null, 255, 255, 255);
	}
}
