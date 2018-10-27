/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.impl;


/**
 * スケジュール定義情報を格納するクラス<br/>
 */
public class SchedulerInfo {

	// スケジューラ名およびグループ名
	public final String name;
	public final String group;

	// スケジューラの開始日時、前回実行日時、次回予定日時
	public final long startTime;
	public final long previousFireTime;
	public final long nextFireTime;

	// pauseされた状態かどうかを示すフラグ
	public final boolean isPaused;

	public SchedulerInfo(String name, String group, long startTime, long previousFireTime, long nextFireTime, boolean isPaused) {
		this.name = name;
		this.group = group;
		this.startTime = startTime;
		this.previousFireTime = previousFireTime;
		this.nextFireTime = nextFireTime;
		this.isPaused = isPaused;
	}
}
