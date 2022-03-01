/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.rpa;

import java.io.File;

import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;

public class ReadingStatus extends AbstractReadingStatus<RpaMonitorInfoWrapper> {

	public ReadingStatus(AbstractFileMonitorManager<RpaMonitorInfoWrapper> fileMonitorManager, String monitorId, File filePath, int firstPartDataCheckSize,
			File rsFilePath, boolean tail) {
		super(fileMonitorManager, monitorId, filePath, firstPartDataCheckSize, rsFilePath, tail);
	}

}
