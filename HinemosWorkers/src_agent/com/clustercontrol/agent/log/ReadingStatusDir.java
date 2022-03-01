/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.log;

import java.io.File;

import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusDir;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusRoot;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.MessageConstant;

public class ReadingStatusDir extends AbstractReadingStatusDir<MonitorInfoWrapper> {

	public ReadingStatusDir(AbstractFileMonitorManager<MonitorInfoWrapper> fileMonitorManager, String fileRsStatus,
			String dirPrefix, String monitorTypeDir, String monitorType,
			AbstractReadingStatusRoot<MonitorInfoWrapper> parent, MonitorInfoWrapper wrapper, String basePath,
			FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, fileRsStatus, dirPrefix, monitorTypeDir, monitorType, parent, wrapper, basePath,
				fileMonitorConfig, true);
	}

	@Override
	public AbstractReadingStatus<MonitorInfoWrapper> createReadingStatus(String monitorId, File filePath,
			int firstPartDataCheckSize, File rsFilePath, boolean tail) {
		boolean isTail = tail;
		if(wrapper.getMonitorInfo().getSdmlMonitorTypeId() != null){
			// SDMLで自動生成された監視の場合は初回先頭から読む
			isTail = false;
		}

		return new ReadingStatus(fileMonitorManager, monitorId, filePath, firstPartDataCheckSize, rsFilePath, isTail);
	}

	@Override
	public void sendMessageByMaxFilesOver() {
		fileMonitorManager.sendMessage(null, PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE_TOO_MANY_FILES.getMessage(), "too many files for logfile.", wrapper);
	}
}
