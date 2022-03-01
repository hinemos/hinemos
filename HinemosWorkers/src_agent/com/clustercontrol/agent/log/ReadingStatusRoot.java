/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.log;

import java.util.List;

import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusDir;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusRoot;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;

/**
 * 各ファイルの読み込み状態を格納するクラス。
 */
public class ReadingStatusRoot extends AbstractReadingStatusRoot<MonitorInfoWrapper> {

	public ReadingStatusRoot(AbstractFileMonitorManager<MonitorInfoWrapper> fileMonitorManager, String fileRsStatus, String dirPrefix, String monitorTypeDir, String monitorType,
			List<MonitorInfoWrapper> miList, String baseDirectory, FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, fileRsStatus, dirPrefix, monitorTypeDir, monitorType, miList, baseDirectory, fileMonitorConfig);
	}

	@Override
	public AbstractReadingStatusDir<MonitorInfoWrapper> createReadingStatusDir(MonitorInfoWrapper wrapper, String basePath,
			FileMonitorConfig fileMonitorConfig) {
		return new ReadingStatusDir(fileMonitorManager, fileRsStatus, dirPrefix, monitorTypeDir, monitorType, this,
				wrapper, basePath, fileMonitorConfig);
	}

}
