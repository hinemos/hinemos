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
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusDir;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusRoot;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.MessageConstant;

public class ReadingStatusDir extends AbstractReadingStatusDir<RpaMonitorInfoWrapper> {

	public ReadingStatusDir(AbstractFileMonitorManager<RpaMonitorInfoWrapper> fileMonitorManager, String fileRsStatus, String dirPrefix, String monitorTypeDir,
			String monitorType, AbstractReadingStatusRoot<RpaMonitorInfoWrapper> parent, RpaMonitorInfoWrapper wrapper, String basePath,
			FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, fileRsStatus, dirPrefix, monitorTypeDir, monitorType, parent, wrapper, basePath, fileMonitorConfig, true);
	}

	@Override
	public void sendMessageByMaxFilesOver() {
		fileMonitorManager.sendMessage(null, PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_RPA_LOG_FILE_TOO_MANY_FILES.getMessage(), "too many files for rpalogfile.", wrapper);
		
	}

	@Override
	public AbstractReadingStatus<RpaMonitorInfoWrapper> createReadingStatus(String monitorId, File filePath, int firstPartDataCheckSize, File rsFilePath, boolean tail) {
		return new ReadingStatus(fileMonitorManager, monitorId, filePath, firstPartDataCheckSize, rsFilePath, tail);
	}

}
