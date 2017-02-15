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

import com.clustercontrol.bean.PriorityConstant;

/**
 * ステータス情報の存続期間の定義を定数として格納するクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class StatusExpirationConstant {

	/** ステータス情報の状態：存続期間経過（種別）。 */
	public static final int TYPE_EXPIRATION = 10;

	/** 存続期間経過後の処理：削除（種別）。 */
	public static final int TYPE_DELETE = 11;

	/** 存続期間経過後の処理：更新（種別）。 */
	public static final int TYPE_UPDATE = 12;

	/** 存続期間経過後の更新時の重要度：危険（種別）。 */
	public static final int TYPE_CRITICAL = PriorityConstant.TYPE_CRITICAL;

	/** 存続期間経過後の更新時の重要度：警告（種別）。 */
	public static final int TYPE_WARNING = PriorityConstant.TYPE_WARNING;

	/** 存続期間経過後の更新時の重要度：通知（種別）。 */
	public static final int TYPE_INFO = PriorityConstant.TYPE_INFO;

	/** 存続期間経過後の更新時の重要度：不明（種別）。 */
	public static final int TYPE_UNKNOWN = PriorityConstant.TYPE_UNKNOWN;

	/** 存続期間経過後の更新時の重要度：なし（種別）。 */
	public static final int TYPE_NONE = PriorityConstant.TYPE_NONE;
}