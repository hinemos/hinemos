/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log.util;

import java.io.File;

import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;

/**
 * 読み込み状態情報を格納し、ファイルへ保存する。
 *
 */
public class CloudLogReadingStatus extends AbstractReadingStatus<MonitorInfoWrapper> {

	public CloudLogReadingStatus(AbstractFileMonitorManager<MonitorInfoWrapper> fileMonitorManager, String monitorId,
			File filePath, int firstPartDataCheckSize, File rsFilePath, boolean tail) {
		super(fileMonitorManager, monitorId, filePath, firstPartDataCheckSize, rsFilePath, tail);
	}
}
