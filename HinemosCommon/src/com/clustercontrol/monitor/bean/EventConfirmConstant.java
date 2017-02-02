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
 * イベント通知の状態の定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class EventConfirmConstant {
	/** 確認済（種別）。 */
	public static final int TYPE_CONFIRMED = ConfirmConstant.TYPE_CONFIRMED;

	/** 未確認（種別）。 */
	public static final int TYPE_UNCONFIRMED = ConfirmConstant.TYPE_UNCONFIRMED;

	/** 破棄（種別）。 */
	public static final int TYPE_DESTRUCTION = 2;

}