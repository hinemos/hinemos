/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;

/**
 * セルフチェック機能の処理実装インターフェース
 */
public interface SelfCheckMonitor {

	public void execute();

	public String toString();

	public String getMonitorId();

}
