/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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