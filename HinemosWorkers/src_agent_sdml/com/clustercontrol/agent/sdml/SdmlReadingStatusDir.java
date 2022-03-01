/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.sdml;

import java.io.File;

import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusDir;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusRoot;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.MessageConstant;

public class SdmlReadingStatusDir extends AbstractReadingStatusDir<SdmlFileMonitorInfoWrapper> {

	public SdmlReadingStatusDir(AbstractFileMonitorManager<SdmlFileMonitorInfoWrapper> fileMonitorManager,
			String fileRsStatus, String dirPrefix, String monitorTypeDir, String monitorType,
			AbstractReadingStatusRoot<SdmlFileMonitorInfoWrapper> parent, SdmlFileMonitorInfoWrapper wrapper,
			String basePath, FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, fileRsStatus, dirPrefix, monitorTypeDir, monitorType, parent, wrapper, basePath,
				fileMonitorConfig, false);
	}

	@Override
	public AbstractReadingStatus<SdmlFileMonitorInfoWrapper> createReadingStatus(String monitorId, File filePath,
			int firstPartDataCheckSize, File rsFilePath, boolean tail) {
		return new SdmlReadingStatus(fileMonitorManager, monitorId, filePath, firstPartDataCheckSize, rsFilePath, tail);
	}

	@Override
	public void sendMessageByMaxFilesOver() {
		fileMonitorManager.sendMessage(null, PriorityConstant.TYPE_WARNING, wrapper.getApplication(),
				MessageConstant.SDML_MSG_LOG_READER_TOO_MANY_FILES.getMessage(), "", wrapper);
	}

}
