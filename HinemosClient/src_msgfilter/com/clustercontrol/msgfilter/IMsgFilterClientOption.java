/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.msgfilter;

public interface IMsgFilterClientOption {
	public String getUrl();

	public String getPerspectiveId();

	public void stopChecktask();
}
