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
 * ジョブ終了状態の定数クラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class EndStatusColorConstant {
	/** 正常 */
	public static final Color COLOR_NORMAL = new Color(null, 0, 255, 0);

	/** 警告 */
	public static final Color COLOR_WARNING = new Color(null, 255, 255, 0);

	/** 異常 */
	public static final Color COLOR_ABNORMAL = new Color(null, 255, 0, 0);
}
