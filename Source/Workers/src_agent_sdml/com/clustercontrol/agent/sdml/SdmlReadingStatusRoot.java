/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.sdml;

import java.util.List;

import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusDir;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusRoot;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;

public class SdmlReadingStatusRoot extends AbstractReadingStatusRoot<SdmlFileMonitorInfoWrapper> {

	public SdmlReadingStatusRoot(AbstractFileMonitorManager<SdmlFileMonitorInfoWrapper> fileMonitorManager,
			String fileRsStatus, String dirPrefix, String monitorTypeDir, String monitorType,
			List<SdmlFileMonitorInfoWrapper> miList, String baseDirectory, FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, fileRsStatus, dirPrefix, monitorTypeDir, monitorType, miList, baseDirectory,
				fileMonitorConfig);
	}

	@Override
	public AbstractReadingStatusDir<SdmlFileMonitorInfoWrapper> createReadingStatusDir(
			SdmlFileMonitorInfoWrapper wrapper, String basePath, FileMonitorConfig fileMonitorConfig) {
		return new SdmlReadingStatusDir(fileMonitorManager, fileRsStatus, dirPrefix, monitorTypeDir, monitorType, this, wrapper,
				basePath, fileMonitorConfig);
	}

}
