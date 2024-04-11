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
	private boolean m_isTail = true;

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
		// シナリオログの監視の場合は、シナリオジョブ実行開始後に作成されたファイルは先頭から、開始前に作成済みのファイルは末尾から監視をスタート
		return new ReadingStatus(fileMonitorManager, monitorId, filePath, firstPartDataCheckSize, rsFilePath, m_isTail);
	}
	
	/**
	 * 終了値判定対象ファイルを末尾から読み込むか否かを設定する
	 * @param isTail
	 * 		true：末尾から読み込み
	 * 		false：先頭から読み込み
	 */
	public void setIsTail(boolean isTail) {
		this.m_isTail = isTail;
	}
}
