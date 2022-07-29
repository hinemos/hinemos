/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa.scenariojob;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusDir;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusRoot;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.agent.log.ReadingStatus;

/**
 * 各ファイルの読み込み状態を格納するクラス。（シナリオジョブ向けログファイル監視用）
 */
public class ScenarioLogReadingStatusDir extends AbstractReadingStatusDir<MonitorInfoWrapper> {
	private static Log m_log = LogFactory.getLog(ScenarioLogReadingStatusDir.class);

	public ScenarioLogReadingStatusDir(AbstractFileMonitorManager<MonitorInfoWrapper> fileMonitorManager,
			String fileRsStatus, String dirPrefix, String monitorTypeDir, String monitorType,
			AbstractReadingStatusRoot<MonitorInfoWrapper> parent, MonitorInfoWrapper wrapper, String basePath,
			FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, fileRsStatus, dirPrefix, monitorTypeDir, monitorType, parent, wrapper, basePath,
				fileMonitorConfig, true);
	}

	@Override
	public void sendMessageByMaxFilesOver() {
		// 通知無し 空実装
	}

	@Override
	public AbstractReadingStatus<MonitorInfoWrapper> createReadingStatus(String monitorId, File filePath,
			int firstPartDataCheckSize, File rsFilePath, boolean tail) {
		// シナリオログの監視の場合は無条件にファイル末尾から監視をスタート（ジョブ開始時点からログを走査したいので）
		boolean isTail = true;
		return new ReadingStatus(fileMonitorManager, monitorId, filePath, firstPartDataCheckSize, rsFilePath, isTail);
	}
}
