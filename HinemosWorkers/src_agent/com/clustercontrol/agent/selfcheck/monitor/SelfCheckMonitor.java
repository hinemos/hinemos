/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.selfcheck.monitor;

/**
 * セルフチェック機能の処理実装インターフェース
 */
public interface SelfCheckMonitor {

	public void execute();

	@Override
	public String toString();

	public String getMonitorId();

}
