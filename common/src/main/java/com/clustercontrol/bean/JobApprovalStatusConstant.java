/*

Copyright (C) 2016 NTT DATA Corporation

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
 * 承認状態の定数クラス<BR>
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class JobApprovalStatusConstant {
	
	// 承認ビューで降順表示するため、「承認待＞承認済」の順で設定
	
	/** 承認待(状態の種別) */
	public static final int TYPE_PENDING = 5;

	/** 未承認(状態の種別) */
	public static final int TYPE_STILL = 4;

	/** 中断中(状態の種別) */
	public static final int TYPE_SUSPEND = 3;

	/** 停止(状態の種別) */
	public static final int TYPE_STOP = 2;

	/** 承認済(状態の種別) */
	public static final int TYPE_FINISHED = 1;
	
}