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
 * 未入力のカラムを知らせる文字色を規定する定数クラス<BR>
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class PropertyFieldColorConstant {
	/** 未入力の項目 **/
	public static final Color COLOR_EMPTY= new Color(null, 128, 128, 128);

	/** 入力済みの項目 **/
	public static final Color COLOR_FILLED = null;

}
