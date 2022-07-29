/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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