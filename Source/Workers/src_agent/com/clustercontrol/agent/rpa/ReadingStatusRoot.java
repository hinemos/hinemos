/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.rpa;

import java.util.List;

import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusDir;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusRoot;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;

/**
 * 各ファイルの読み込み状態を格納するクラス。
 */
public class ReadingStatusRoot extends AbstractReadingStatusRoot<RpaMonitorInfoWrapper> {

	public ReadingStatusRoot(AbstractFileMonitorManager<RpaMonitorInfoWrapper> fileMonitorManager, String fileRsStatus, String dirPrefix, String monitorTypeDir,
			String monitorType, List<RpaMonitorInfoWrapper> miList, String baseDirectory, FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, fileRsStatus, dirPrefix, monitorTypeDir, monitorType, miList, baseDirectory, fileMonitorConfig);
	}

	@Override
	public AbstractReadingStatusDir<RpaMonitorInfoWrapper> createReadingStatusDir(RpaMonitorInfoWrapper wrapper, String basePath, FileMonitorConfig fileMonitorConfig) {
		return new ReadingStatusDir(fileMonitorManager, fileRsStatus, dirPrefix, monitorTypeDir, monitorType, this,
				wrapper, basePath, fileMonitorConfig);
	}

}
