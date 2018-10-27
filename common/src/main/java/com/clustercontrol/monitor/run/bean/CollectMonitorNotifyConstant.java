/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.bean;

/**
 * 将来予測監視の通知グループIDで使用する定数を格納するクラス<BR>
 * 
 * @version 6.1.0
 */
public class CollectMonitorNotifyConstant {
	/** 通知グループID（将来予測用）Prefix */
	public static final String PREDICTION_NOTIFY_GROUPID_PREFIX = MonitorNumericType.TYPE_PREDICTION.getType() + "_";

	/** 通知グループID（変化点用）Prefix */
	public static final String CHANGE_NOTIFY_GROUPID_PREFIX = MonitorNumericType.TYPE_CHANGE.getType() + "_";
}