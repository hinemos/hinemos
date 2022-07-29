/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa.scenariojob;

import java.util.List;

import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusRoot;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;

/**
 * 各ファイルの読み込み状態を格納するクラス。
 */
public class ScenarioLogReadingStatusRoot extends AbstractReadingStatusRoot<MonitorInfoWrapper> {

	public ScenarioLogReadingStatusRoot(AbstractFileMonitorManager<MonitorInfoWrapper> fileMonitorManager,
			String fileRsStatus, String dirPrefix, String monitorTypeDir, String monitorType,
			List<MonitorInfoWrapper> miList, String baseDirectory, FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, fileRsStatus, dirPrefix, monitorTypeDir, monitorType, miList, baseDirectory,
				fileMonitorConfig);
	}

	@Override
	public ScenarioLogReadingStatusDir createReadingStatusDir(MonitorInfoWrapper wrapper, String basePath,
			FileMonitorConfig fileMonitorConfig) {
		return new ScenarioLogReadingStatusDir(fileMonitorManager, fileRsStatus, dirPrefix, monitorTypeDir, monitorType,
				this, wrapper, basePath, fileMonitorConfig);
	}

}
