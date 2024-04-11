/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.cloud.log.CloudLogMonitorUtil;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitor;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatusRoot;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;

public class CloudLogfileMonitorManager extends AbstractFileMonitorManager<MonitorInfoWrapper> {
	private static CloudLogfileMonitorManager instance = new CloudLogfileMonitorManager(
			CloudLogfileMonitorConfig.getInstance());
	private static Log log = LogFactory.getLog(CloudLogfileMonitorManager.class);

	public static CloudLogfileMonitorManager getInstance() {
		return instance;
	}

	private CloudLogfileMonitorManager(FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorConfig);
	}

	/**
	 * 監視設定IDから必要なファイル監視設定をリストに詰めて返却します。
	 * 
	 * @param id
	 * @return
	 */
	public List<CloudLogfileMonitor> getCloudLogFileMonitor(String id) {

		synchronized (this) {
			ArrayList<CloudLogfileMonitor> list = new ArrayList<CloudLogfileMonitor>();
			refresh();

			// ファイル監視設定（監視対象ファイルごと）の一覧から
			// 監視設定IDに対応するものを取得
			for (Entry<String, AbstractFileMonitor<MonitorInfoWrapper>> e : logfileMonitorCache.entrySet()) {
				CloudLogfileMonitor mon = ((CloudLogfileMonitor) e.getValue());
				log.debug("getCloudLogFileMonitor(): found keys:" + e.getKey());
				log.debug("getCloudLogFileMonitor(): found id:" + mon.getMonitorId());
				if (mon.getMonitorId().equals(id)) {
					list.add(mon);
				}
			}
			return list;
		}
	}
	
	/**
	 * ファイル監視の対象となっているファイルをクローズします。
	 * @param id
	 */
	public void cleanCloudLogMonitorFiles(String id) {
		List<CloudLogfileMonitor> monlist = getCloudLogFileMonitor(id);
		for (CloudLogfileMonitor mon : monlist) {
			mon.clean();
		}
	}
	
	/**
	 * リーディングステータスの更新を行います。
	 */
	public void clearReadingStatus() {
		synchronized (this) {
			refresh();
		}
	}

	@Override
	public String getReadingStatusStorePath() {
		String home = Agent.getAgentHome();
		String storepath = new File(new File(home), "cloudlog_readingstatus").getAbsolutePath();
		return storepath;
	}

	@Override
	public AbstractFileMonitor<MonitorInfoWrapper> createFileMonitor(MonitorInfoWrapper monitorInfo,
			AbstractReadingStatus<MonitorInfoWrapper> status, FileMonitorConfig fileMonitorConfig) {
		return new CloudLogfileMonitor(this, monitorInfo, status, fileMonitorConfig);
	}

	@Override
	public AbstractReadingStatusRoot<MonitorInfoWrapper> createReadingStatusRoot(List<MonitorInfoWrapper> miList,
			String baseDirectory, FileMonitorConfig fileMonitorConfig) {
		return new CloudLogReadingStatusRoot(this, "rstatus.json", "rs_", "monitor_type", "cloudlog", miList,
				baseDirectory, fileMonitorConfig);
	}

	@Override
	public void sendMessageInner(String filePath, int priority, String app, String msg, String msgOrg, String monitorId,
			AgtRunInstructionInfoRequest runInstructionInfo) {

		CloudLogMonitorUtil.sendMessage(priority, app, msg, msgOrg, monitorId, runInstructionInfo);
	}

	@Override
	public void sendMessageInner(String filePath, int priority, String app, String msg, String msgOrg,
			MonitorInfoWrapper monitorInfoWrapper) {
		sendMessageInner(filePath, priority, app, msg, msgOrg, monitorInfoWrapper.getId(),
				monitorInfoWrapper.getRunInstructionInfoReq());
	}

}
