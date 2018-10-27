/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.bean;


/**
 * Quartz関連の定義を定数として格納するクラスです。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class QuartzConstant {

	/**
	 *  メンテナンスのグループ。<BR>
	 *  メンテナンス機能のスケジュール実行を呼び出すQuartzのメンテナンスのグループ名です。
	 */
	public static final String GROUP_NAME = "HUBTransfer";

	/**
	 *  Quartzから呼び出すメソッド名。<BR>
	 *  メンテナンス機能のスケジュール実行を行う Session Bean のメソッド名です。
	 */
	public static final String METHOD_NAME = "scheduleRunTransfer";
}
