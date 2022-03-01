/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;


/**
 * ジョブの開始遅延及び終了遅延における通知用の定数を定義するクラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class DelayNotifyConstant {
	/** 通知・操作なし */
	public static final int NONE = 0;
	/** 開始遅延通知済み */
	public static final int START = 1;
	/** 終了遅延通知済み */
	public static final int END = 2;
	/** 開始＆終了遅延通知済み */
	public static final int START_AND_END = 3;
	/** 停止[コマンド]操作済み */
	public static final int STOP_AT_ONCE = 10;
	/** 停止[中断]操作済み */
	public static final int STOP_SUSPEND = 20;
	/** 停止[状態指定]操作済み */
	public static final int STOP_SET_END_VALUE = 30;
	/** 停止[状態指定](強制)操作済み */
	public static final int STOP_SET_END_VALUE_FORCE = 40;

	/**
	 * 遅延通知状態から通知済みフラグを取り出し、通知済みフラグを返します。<BR>
	 * 
	 * @param notify 遅延通知状態
	 * @return 通知済みフラグ
	 */
	public static int getNotify(int notify){
		if(notify >= STOP_SET_END_VALUE_FORCE){
			notify = notify - STOP_SET_END_VALUE_FORCE;
		}
		if(notify >= STOP_SET_END_VALUE){
			notify = notify - STOP_SET_END_VALUE;
		}
		else if(notify >= STOP_SUSPEND){
			notify = notify - STOP_SUSPEND;
		}
		else if(notify >= STOP_AT_ONCE){
			notify = notify - STOP_AT_ONCE;
		}
		return notify;
	}

	/**
	 * 遅延通知状態から操作済みフラグを取り出し、操作済みフラグを返します。<BR>
	 * 
	 * @param notify 遅延通知状態
	 * @return 操作済みフラグ
	 */
	public static int getOperation(int notify){
		if(notify >= STOP_SET_END_VALUE_FORCE){
			return STOP_SET_END_VALUE_FORCE;
		}
		if(notify >= STOP_SET_END_VALUE){
			return STOP_SET_END_VALUE;
		}
		else if(notify >= STOP_SUSPEND){
			return STOP_SUSPEND;
		}
		else if(notify >= STOP_AT_ONCE){
			return STOP_AT_ONCE;
		}
		return NONE;
	}

	/**
	 * 遅延通知状態に操作済みフラグを追加します。<BR>
	 * <p>
	 * 遅延通知状態から、通知済みフラグを取り出し、<BR>
	 * 通知済みフラグに、操作済みフラグを足して、遅延通知状態を作成し返します。
	 * 
	 * @param notify 遅延通知状態
	 * @param operation 操作済みフラグ
	 * @return 遅延通知状態
	 */
	public static int addOperation(int notify, int operation){
		notify = getNotify(notify);
		return notify + operation;
	}
}
