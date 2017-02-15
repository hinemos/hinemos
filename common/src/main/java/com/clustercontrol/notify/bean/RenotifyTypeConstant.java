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

package com.clustercontrol.notify.bean;

/**
 * 抑制間隔の定義を定数として格納するクラス<BR>
 *
 * @version 2.1.0
 * @since 1.0.0
 */
public class RenotifyTypeConstant {
	/** 常に通知する（種別）。 */
	public static final int TYPE_ALWAYS_NOTIFY = 0;

	/** 期間で抑制する（分単位）（種別）。 */
	public static final int TYPE_PERIOD = 1;

	/** 再通知しない（種別）。 */
	public static final int TYPE_NO_NOTIFY = 2;
}